package com.rmpestano.forge.crud;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.forge.parser.java.JavaClass;
import org.jboss.forge.project.facets.JavaSourceFacet;
import org.jboss.forge.project.packaging.PackagingType;
import org.jboss.forge.shell.util.ConstraintInspector;
import org.jboss.forge.shell.util.Packages;
import org.jboss.forge.spec.javaee.PersistenceFacet;
import org.jboss.forge.spec.javaee.jpa.EntityPlugin;
import org.jboss.forge.test.AbstractShellTest;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class CrudPluginTest extends AbstractShellTest {
    @Deployment
    public static JavaArchive getDeployment() {
        return AbstractShellTest.getDeployment()
                .addPackages(true, CrudPlugin.class.getPackage());
    }

    @Before
    @Override
    public void beforeTest() throws Exception {
        super.beforeTest();
        initializeProject(PackagingType.WAR);
        queueInputLines("", "", "");
        getShell().execute("persistence setup --provider HIBERNATE --container JBOSS_AS7");
        String entityName = "Person";
        queueInputLines("");
        getShell().execute(ConstraintInspector.getName(EntityPlugin.class) + " --named " + entityName);
        String pkg = getProject().getFacet(PersistenceFacet.class).getEntityPackage() + "." + entityName;
        String path = Packages.toFileSyntax(pkg) + ".java";
        JavaClass javaClass = (JavaClass) getProject().getFacet(JavaSourceFacet.class).getJavaResource(path).getJavaSource();
        assertNotNull(javaClass);
        queueInputLines("n", "");
        getShell().execute("beans setup");
        queueInputLines("y");
        getShell().execute("crud setup");
    }

    @Test
    public void crudTest() throws Exception {
        assertNotNull(getProject());
        assertTrue(getProject().hasFacet(CrudFacet.class));
        JavaSourceFacet sourceFacet = getProject().getFacet(JavaSourceFacet.class);
        String pkg = sourceFacet.getBasePackage() + ".crud" + ".Crud";
        String path = Packages.toFileSyntax(pkg) + ".java";
        JavaClass crudClass = (JavaClass) getProject().getFacet(JavaSourceFacet.class).getJavaResource(path).getJavaSource();
        assertNotNull(crudClass);
        assertFalse(crudClass.hasSyntaxErrors());
    }

    @Test
    public void serviceTest() throws Exception {
        assertNotNull(getProject());
        assertTrue(getProject().hasFacet(CrudFacet.class));
        String pkg = getProject().getFacet(PersistenceFacet.class).getEntityPackage() + ".Person";
        String path = Packages.toFileSyntax(pkg) + ".java";
        JavaClass javaClass = (JavaClass) getProject().getFacet(JavaSourceFacet.class).getJavaResource(path).getJavaSource();
        assertNotNull(javaClass);
        getShell().execute("crud service-from-entity --entity "+pkg);
        JavaSourceFacet sourceFacet = getProject().getFacet(JavaSourceFacet.class);
        String servicePackage = sourceFacet.getBasePackage() + ".service" + ".PersonService";
        String servicePath = Packages.toFileSyntax(servicePackage) + ".java";
        JavaClass serviceClass = (JavaClass) getProject().getFacet(JavaSourceFacet.class).getJavaResource(servicePath).getJavaSource();
        assertNotNull(serviceClass);
        assertFalse(serviceClass.hasSyntaxErrors());
    }


}
