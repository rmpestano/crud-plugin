package com.rmpestano.forge.crud;

import org.jboss.forge.parser.JavaParser;
import org.jboss.forge.parser.java.JavaClass;
import org.jboss.forge.project.Project;
import org.jboss.forge.project.facets.JavaSourceFacet;
import org.jboss.forge.project.facets.events.InstallFacets;
import org.jboss.forge.resources.DirectoryResource;
import org.jboss.forge.resources.FileResource;
import org.jboss.forge.resources.Resource;
import org.jboss.forge.resources.ResourceFilter;
import org.jboss.forge.resources.java.JavaResource;
import org.jboss.forge.shell.PromptType;
import org.jboss.forge.shell.ShellMessages;
import org.jboss.forge.shell.ShellPrompt;
import org.jboss.forge.shell.plugins.*;
import org.jboss.forge.shell.util.Packages;
import org.jboss.forge.spec.javaee.EJBFacet;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
@Alias("crud")
@RequiresFacet(CrudFacet.class)
public class CrudPlugin implements Plugin {

    @Inject
    private ShellPrompt prompt;

    @Inject
    private Project project;

    @Inject
    private JavaSourceFacet javaSource;

    @Inject
    private FreemarkerTemplateProcessor processor;

    @Inject
    private Event<InstallFacets> request;

    private String crudPackage;


    @SetupCommand
    public void setup(final PipeOut out,
                      @Option(name = "topLevelPackage",
                              description = "The top-level java package for CRUD base classes [e.g: \"com.example.project.crud\"] ",
                              type = PromptType.JAVA_PACKAGE) final String topLevelPackage) {

        String javaPackage;
        if (topLevelPackage == null) {
            javaPackage = "com.forge." + Packages.toValidPackageName("crud");
        } else {
            javaPackage = topLevelPackage;
        }
        installCrud();
        createBaseClasses(javaPackage);

        if (project.hasFacet(CrudFacet.class)) {
            ShellMessages.success(out, "CRUD is installed.");
        }
    }


    private void createBaseClasses(String javaPackage) {
        Map<Object, Object> params = new HashMap<Object, Object>();
        params.put("package", javaPackage);
        String crudOut = processor.processTemplate(params, "template/Crud.jv");
        JavaClass crudClass = JavaParser.parse(JavaClass.class, crudOut);
        try {
            javaSource.saveJavaSource(crudClass);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }



    @Command(value = "service-from-entity", help = "Creates a crud service from entity")
    public void service( @Option(required = true, type = PromptType.JAVA_CLASS, description = "entity to generate the crud service") JavaResource entity,
                         @Option(name = "topLevelPackage",
                                 description = "The top-level java package for the crud service [e.g: \"com.example.project.crud\"] ",
                                 type = PromptType.JAVA_PACKAGE) final String topLevelPackage,
                         PipeOut out) {

        if (!project.hasFacet(EJBFacet.class)) {
            request.fire(new InstallFacets(true, EJBFacet.class));
        }

        String javaPackage;

        if (topLevelPackage == null) {
            javaPackage = "com.forge." + Packages.toValidPackageName("service");
        } else {
            javaPackage = topLevelPackage;
        }

        try {
            createService(entity, javaPackage);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void createService(JavaResource entity, String javaPackage) throws FileNotFoundException {
        Map<Object, Object> params = new HashMap<Object, Object>();
        params.put("package", javaPackage);
        String entityClass = entity.getJavaSource().getName();
        params.put("entityClass", entityClass);
        params.put("entityPackage", entity.getJavaSource().getPackage()+"."+entityClass);
        String camelName = ((Character)entityClass.charAt(0)).toString().toLowerCase() +  entityClass.substring(1);
        params.put("entityName", camelName);
        findCrudPackage(project.getProjectRoot());
        params.put("crudPackage", crudPackage);
        String serviceOut = processor.processTemplate(params, "template/Service.jv");
        JavaClass serviceClass = JavaParser.parse(JavaClass.class, serviceOut);
        try {
            javaSource.saveJavaSource(serviceClass);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    private void findCrudPackage(DirectoryResource projectRoot) {
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
                try {
                    this.crudPackage = ((JavaResource)candidates.get(0)).getJavaSource().getPackage()+".Crud";
                    return;
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    throw new RuntimeException("Crud.java not found, execute crud setup before");
                }
            }
            else{
                for (DirectoryResource child : children) {
                     findCrudPackage(child);
                }
            }

    }

    private void installCrud() {
        if (!project.hasFacet(CrudFacet.class)) {
            request.fire(new InstallFacets(CrudFacet.class));
        }
    }
}
