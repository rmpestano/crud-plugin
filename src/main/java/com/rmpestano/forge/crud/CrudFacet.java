package com.rmpestano.forge.crud;

import org.jboss.forge.project.dependencies.Dependency;
import org.jboss.forge.project.dependencies.DependencyBuilder;
import org.jboss.forge.project.dependencies.DependencyInstaller;
import org.jboss.forge.project.dependencies.ScopeType;
import org.jboss.forge.project.facets.BaseFacet;
import org.jboss.forge.project.facets.DependencyFacet;
import org.jboss.forge.project.packaging.PackagingType;
import org.jboss.forge.resources.DirectoryResource;
import org.jboss.forge.resources.FileResource;
import org.jboss.forge.resources.Resource;
import org.jboss.forge.resources.ResourceFilter;
import org.jboss.forge.shell.plugins.RequiresFacet;
import org.jboss.forge.shell.plugins.RequiresPackagingType;
import org.jboss.forge.spec.javaee.CDIFacet;
import org.jboss.forge.spec.javaee.PersistenceFacet;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;


@RequiresFacet({
        DependencyFacet.class, PersistenceFacet.class, CDIFacet.class})
@RequiresPackagingType({PackagingType.WAR})
public class CrudFacet extends BaseFacet {

    public static final Dependency HIBERNATE_ENTITY_MANAGER = DependencyBuilder.create("org.hibernate:hibernate-entitymanager");

    public static final Dependency CDI_API = DependencyBuilder.create("javax.enterprise:cdi-api:1.1");

    public static final Dependency CDI_INJECT = DependencyBuilder.create("javax.inject:javax.inject:1");

    private final DependencyInstaller installer;

    private Boolean crudCreated;//Crud.java must exist to enable this facet, it is created via setup command


    @Inject
    public CrudFacet(final DependencyInstaller installer) {
        this.installer = installer;
    }

    @Override
    public boolean install() {
        DependencyFacet deps = project.getFacet(DependencyFacet.class);
        if (!deps.hasEffectiveManagedDependency(HIBERNATE_ENTITY_MANAGER)) {
            installer.installManaged(project, HIBERNATE_ENTITY_MANAGER);
        }

        if (!deps.hasDirectDependency(HIBERNATE_ENTITY_MANAGER)) {
            installer.install(project, HIBERNATE_ENTITY_MANAGER, ScopeType.PROVIDED);
        }

        //CDI will not be on managed dependencies section
        if (!deps.hasDirectDependency(CDI_API)) {
            installer.install(project, CDI_API, ScopeType.PROVIDED);
        }

        if (!deps.hasDirectDependency(CDI_INJECT)) {
            installer.install(project, CDI_INJECT, ScopeType.PROVIDED);
        }

        crudIsCreated(getProject().getProjectRoot());
        return crudCreated != null && crudCreated;
    }




    @Override
    public boolean isInstalled() {
        return crudCreated != null || install();
    }

    public boolean crudIsCreated(DirectoryResource projectRoot) {
        final List<DirectoryResource> children = new ArrayList<DirectoryResource>();
        List<Resource<?>> candidates = projectRoot.listResources(new ResourceFilter() {
            @Override
            public boolean accept(Resource<?> resource) {
                if(resource instanceof DirectoryResource){
                    children.add(resource.reify(DirectoryResource.class));
                    return false;
                }
                return resource instanceof FileResource && resource.getName().equals("Crud.java");
            }
        });

        if(candidates != null && !candidates.isEmpty()){
                crudCreated = Boolean.TRUE;
        }
        else{
            for (DirectoryResource child : children) {
                crudIsCreated(child);
            }
        }

        return Boolean.FALSE;
    }
}
