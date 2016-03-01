/**
 * Copyright © 2013 Instituto Superior Técnico
 *
 * This file is part of FenixEdu IST CMS Components.
 *
 * FenixEdu IST CMS Components is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FenixEdu IST CMS Components is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FenixEdu IST CMS Components.  If not, see <http://www.gnu.org/licenses/>.
 */
package pt.ist.fenixedu.cmscomponents.ui.spring;

import static java.util.Comparator.comparing;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.fenixedu.academic.domain.accessControl.StudentGroup;
import org.fenixedu.academic.domain.accessControl.StudentSharingDegreeOfCompetenceOfExecutionCourseGroup;
import org.fenixedu.academic.domain.accessControl.StudentSharingDegreeOfExecutionCourseGroup;
import org.fenixedu.academic.domain.accessControl.TeacherGroup;
import org.fenixedu.bennu.core.groups.AnyoneGroup;
import org.fenixedu.bennu.core.groups.Group;
import org.fenixedu.bennu.core.groups.LoggedGroup;
import org.fenixedu.bennu.core.groups.UserGroup;
import org.fenixedu.bennu.core.security.Authenticate;
import org.fenixedu.bennu.io.domain.GroupBasedFile;
import org.fenixedu.bennu.io.servlets.FileDownloadServlet;
import org.fenixedu.cms.domain.Category;
import org.fenixedu.cms.domain.Menu;
import org.fenixedu.cms.domain.MenuItem;
import org.fenixedu.cms.domain.Page;
import org.fenixedu.cms.domain.Post;
import org.fenixedu.cms.domain.PostFile;
import org.fenixedu.cms.domain.Site;
import org.fenixedu.cms.domain.component.StaticPost;
import org.fenixedu.commons.i18n.I18N;
import org.fenixedu.commons.i18n.LocalizedString;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.Atomic.TxMode;

@Service
public class PagesAdminService {

    private final Predicate<MenuItem> isStaticPage = menuItem -> menuItem.getPage() != null
            && menuItem.getPage().getComponentsSet().stream().filter(StaticPost.class::isInstance)
                    .map(component -> ((StaticPost) component).getPost()).filter(post -> post != null).findFirst().isPresent();

    protected static Stream<Page> dynamicPages(Site site) {
        return site.getPagesSet().stream().filter(PagesAdminService::isDynamicPage)
                .filter(page -> !site.getInitialPage().equals(page)).sorted(comparing(Page::getName));
    }

    protected static boolean isDynamicPage(Page page) {
        return !page.getComponentsSet().stream().filter(StaticPost.class::isInstance).findAny().isPresent();
    }

    static List<Group> permissionGroups(Site site) {
        if (site.getExecutionCourse()!=null) {
            return ImmutableList.of(
                    AnyoneGroup.get(),
                    LoggedGroup.get(),
                    TeacherGroup.get(site.getExecutionCourse()),
                    TeacherGroup.get(site.getExecutionCourse()).or(StudentGroup.get(site.getExecutionCourse())),
                    StudentSharingDegreeOfExecutionCourseGroup.get(site.getExecutionCourse()),
                    StudentSharingDegreeOfCompetenceOfExecutionCourseGroup.get(site.getExecutionCourse())
            );
        }
        if (site.getHomepageSite()!=null) {
            return ImmutableList.of(
                    AnyoneGroup.get(),
                    LoggedGroup.get(),
                    UserGroup.of(site.getOwner().getUser()));
        }
        return ImmutableList.of(AnyoneGroup.get(), LoggedGroup.get());
    }

    @Atomic(mode = Atomic.TxMode.WRITE)
    protected void delete(MenuItem menuItem) {
        //recursive call to remove associated childrens
        menuItem.getChildrenSorted().forEach(this::delete);
        //deleting a page allready deletes all the associated menu items and components
        menuItem.getPage().delete();
    }

    @Atomic(mode = Atomic.TxMode.WRITE)
    protected Optional<MenuItem> create(Site site, MenuItem parent, LocalizedString name, LocalizedString body) {
        Menu menu = site.getMenusSet().stream().findFirst().orElse(null);
        Page page = Page.create(site, menu, parent, Post.sanitize(name), true, "view", Authenticate.getUser());
        Category category = site.getOrCreateCategoryForSlug("content", new LocalizedString().with(I18N.getLocale(), "Content"));
        Post post = Post.create(site, page, Post.sanitize(name), Post.sanitize(body), category, true, Authenticate.getUser());
        page.addComponents(new StaticPost(post));
        MenuItem menuItem = page.getMenuItemsSet().stream().findFirst().get();
        if (parent != null) {
            parent.add(menuItem);
        } else {
            menu.add(menuItem);
        }
        return Optional.of(menuItem);
    }

    @Atomic(mode = Atomic.TxMode.WRITE)
    protected MenuItem edit(MenuItem menuItem, LocalizedString name, LocalizedString body, Group canViewGroup, Boolean visible) {
        name = Post.sanitize(name);
        body = Post.sanitize(body);
        if (!menuItem.getName().equals(name)) {
            menuItem.setName(name);
        }
        Post post = postForPage(menuItem.getPage());

        if (visible != null) {
            menuItem.getPage().setPublished(visible);
        }

        if (!menuItem.getPage().getName().equals(name)) {
            menuItem.getPage().setName(name);
        }

        if (post.getBody() == null && body != null || post.getBody() != null && !post.getBody().equals(body)) {
            post.setBody(body);
        }
        if (!post.getName().equals(name)) {
            post.setName(name);
        }

        if (canViewGroup != null && !post.getCanViewGroup().equals(canViewGroup)) {
            post.setCanViewGroup(canViewGroup);
        }

        return menuItem;
    }

    @Atomic(mode = TxMode.WRITE)
    protected void moveTo(MenuItem item, MenuItem parent, MenuItem insertAfter) {
        Menu menu = item.getMenu();

        if (insertAfter == null && parent == null) {
            insertAfter = getLastBuiltinContent(menu);
        }

        if (parent == null) {
            MenuItem.fixOrder(menu.getToplevelItemsSorted().collect(Collectors.toList()));
            int newPosition = insertAfter == null ? 0 : insertAfter.getPosition() + 1;
            menu.putAt(item, newPosition);
        } else {
            MenuItem.fixOrder(parent.getChildrenSorted());
            int newPosition = insertAfter == null ? 0 : insertAfter.getPosition() + 1;
            parent.putAt(item, newPosition);
        }
    }

    private MenuItem getLastBuiltinContent(Menu menu) {
        return menu.getToplevelItemsSorted().sorted(Comparator.reverseOrder()).filter(isStaticPage.negate()).findFirst()
                .orElse(null);
    }

    @Atomic(mode = Atomic.TxMode.WRITE)
    protected PostFile addAttachment(String name, MultipartFile attachment, MenuItem menuItem) throws IOException {
        Post post = postForPage(menuItem.getPage());
        GroupBasedFile file =
                new GroupBasedFile(name, attachment.getOriginalFilename(), attachment.getBytes(), AnyoneGroup.get());
        return new PostFile(post, file, false, post.getFilesSet().size());
    }

    private Post postForPage(Page page) {
        return page.getComponentsSet().stream().filter(component -> component instanceof StaticPost)
                .map(component -> ((StaticPost) component).getPost()).filter(post -> post != null).findFirst().get();
    }

    protected JsonObject serialize(Site site) {
        JsonObject data = new JsonObject();
        if (!site.getMenusSet().isEmpty()) {
            Menu menu = site.getMenusSet().stream().filter(m -> !m.getPrivileged()).findFirst().get();
            JsonObject root = new JsonObject();
            root.add("title", site.getName().json());
            root.add("root", new JsonPrimitive(true));
            root.add("isFolder", new JsonPrimitive(true));
            root.add("expanded", new JsonPrimitive(true));
            root.add("key", new JsonPrimitive("null"));

            JsonArray groupsJson = new JsonArray();
            for (Group group : permissionGroups(site)) {
                groupsJson.add(serializeGroup(group));
            }

            JsonArray child = new JsonArray();
            menu.getToplevelItemsSorted().filter(isStaticPage).map(item -> serialize(item, false))
                    .forEach(json -> child.add(json));
            root.add("children", child);
            data.add("root", root);
            data.add("groups", groupsJson);
        }
        return data;
    }

    protected JsonObject serialize(MenuItem item, boolean withBody) {
        JsonObject root = new JsonObject();

        root.add("title", item.getName().json());
        if (item.getParent() != null) {
            root.add("menuItemParentId", new JsonPrimitive(item.getParent().getExternalId()));
        }
        root.add("key", new JsonPrimitive(item.getExternalId()));
        String pageAddress = Optional.ofNullable(item.getUrl()).orElse(item.getPage().getAddress());
        root.add("pageAddress", new JsonPrimitive(pageAddress));
        root.add("position", new JsonPrimitive(item.getPosition()));
        root.add("isFolder", new JsonPrimitive(Optional.ofNullable(item.getFolder()).orElse(false)));
        root.addProperty("visible", item.getPage().isPublished());

        if (withBody) {
            root.add("body", data(item.getMenu().getSite(), item));
        }

        root.add("files", serializeAttachments(item.getPage()));

        if (item.getChildrenSet().size() > 0) {
            root.add("folder", new JsonPrimitive(true));
            JsonArray children = new JsonArray();
            item.getChildrenSorted().stream().filter(isStaticPage).forEach(subitem -> children.add(serialize(subitem, false)));
            root.add("children", children);
        }
        root.addProperty("canViewGroupIndex", canViewGroupIndex(item.getPage(), postForPage(item.getPage()).getCanViewGroup()));

        return root;
    }

    private Integer canViewGroupIndex(Page page, Group group) {
        List<Group> permissionGroups = permissionGroups(page.getSite());
        for (int i = 0; i < permissionGroups.size(); ++i) {
            if (permissionGroups.get(i).equals(group)) {
                return i;
            }
        }
        return 0;
    }

    private JsonObject serializeGroup(Group group) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("name", group.getPresentationName());
        jsonObject.addProperty("expression", group.getExpression());
        return jsonObject;
    }

    protected JsonElement serializeAttachments(Page page) {
        Post post = postForPage(page);
        JsonArray filesJson = new JsonArray();
        post.getAttachmentFilesSorted().map(PostFile::getFiles).forEach(postFile -> {
            JsonObject json = describeFile(page, postFile);
            json.addProperty("visible", true);
            filesJson.add(json);
        });
        if (filesJson.size() > 0) {
            filesJson.get(filesJson.size() - 1).getAsJsonObject().addProperty("last", true);
        }
        post.getEmbeddedFilesSorted().map(PostFile::getFiles).forEach(postFile -> {
            JsonObject json = describeFile(page, postFile);
            json.addProperty("visible", false);
            filesJson.add(json);
        });
        return filesJson;
    }

    protected JsonObject describeFile(Page page, GroupBasedFile file) {
        JsonObject postFileJson = new JsonObject();
        postFileJson.addProperty("name", file.getDisplayName());
        postFileJson.addProperty("filename", file.getFilename());
        postFileJson.addProperty("externalId", file.getExternalId());
        postFileJson.addProperty("creationDate", file.getCreationDate().toString());
        postFileJson.addProperty("contentType", file.getContentType());
        postFileJson.addProperty("size", file.getSize());
        postFileJson.addProperty("downloadUrl", FileDownloadServlet.getDownloadUrl(file));
        postFileJson.addProperty("group", canViewGroupIndex(page, file.getAccessGroup()));
        return postFileJson;
    }

    @Atomic
    protected GroupBasedFile addPostFile(MultipartFile attachment, MenuItem menuItem) throws IOException {
        GroupBasedFile f = new GroupBasedFile(attachment.getOriginalFilename(), attachment.getOriginalFilename(),
                attachment.getBytes(), AnyoneGroup.get());
        Post post = postForPage(menuItem.getPage());
        new PostFile(post, f, true, post.getFilesSet().size());
        return f;
    }

    @Atomic(mode = Atomic.TxMode.WRITE)
    public void delete(MenuItem menuItem, GroupBasedFile file) {
        file.getPostFile().delete();
        file.delete();
    }

    @Atomic(mode = Atomic.TxMode.WRITE)
    public void updateAttachment(MenuItem menuItem, GroupBasedFile attachment, int newPosition, int groupPosition,
            String displayName, boolean visible) {
        if (displayName != null) {
            attachment.setDisplayName(displayName);
        }
        attachment.setAccessGroup(permissionGroups(menuItem.getMenu().getSite()).get(groupPosition));

        Post post = postForPage(menuItem.getPage());
        if (newPosition < 0 || newPosition > post.getFilesSet().size()) {
            newPosition = post.getFilesSet().size();
        }

        new PostFile(post, attachment, true, newPosition);
    }

    protected void copyStaticPage(MenuItem oldMenuItem, Site newSite, Menu newMenu, MenuItem newParent) {
        if (oldMenuItem.getPage() != null) {
            Page oldPage = oldMenuItem.getPage();
            staticPost(oldPage).ifPresent(oldPost -> {
                Page newPage = new Page(newSite, oldPage.getName());
                newPage.setTemplate(newSite.getTheme().templateForType(oldPage.getTemplate().getType()));
                newPage.setCreatedBy(Authenticate.getUser());
                newPage.setPublished(false);

                oldPage.getComponentsSet().stream().filter(component -> component instanceof StaticPost).forEach(component -> {
                    StaticPost staticPostComponent = (StaticPost) component;
                    Post newPost = clonePost(staticPostComponent.getPost(), newSite);
                    newPost.setActive(true);
                    StaticPost newComponent = new StaticPost(newPost);
                    newPage.addComponents(newComponent);
                });

                MenuItem newMenuItem = MenuItem.create(newMenu, newPage, oldMenuItem.getName(), newParent);
                newMenuItem.setPosition(oldMenuItem.getPosition());
                newMenuItem.setUrl(oldMenuItem.getUrl());
                newMenuItem.setFolder(oldMenuItem.getFolder());

                oldMenuItem.getChildrenSet().stream().forEach(child -> copyStaticPage(child, newSite, newMenu, newMenuItem));
            });
        }
    }

    private Post clonePost(Post oldPost, Site newSite) {
        Post newPost = new Post(newSite);
        newPost.setName(oldPost.getName());
        newPost.setBody(oldPost.getBody());
        newPost.setCreationDate(new DateTime());
        newPost.setCreatedBy(Authenticate.getUser());
        newPost.setActive(oldPost.getActive());

        for (Category oldCategory : oldPost.getCategoriesSet()) {
            Category newCategory = newSite.getOrCreateCategoryForSlug(oldCategory.getSlug(), oldCategory.getName());
            newPost.addCategories(newCategory);
        }

        oldPost.getFilesSorted().forEach(postFile -> {
            GroupBasedFile file = postFile.getFiles();
            GroupBasedFile attachmentCopy =
                    new GroupBasedFile(file.getDisplayName(), file.getFilename(), file.getContent(), AnyoneGroup.get());
            new PostFile(newPost, attachmentCopy, postFile.getIsEmbedded(), newPost.getFilesSet().size());

        });
        return newPost;
    }

    private Optional<Post> staticPost(Page page) {
        return page.getComponentsSet().stream().filter(StaticPost.class::isInstance).map(StaticPost.class::cast)
                .map(StaticPost::getPost).findFirst();
    }

    public JsonElement data(Site site, MenuItem item) {
        return postForPage(item.getPage()).getBody() != null ? postForPage(item.getPage()).getBody().json() : new JsonObject();
    }

}
