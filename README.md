[JBoss Forge](forge.jboss.org/) crud generator plugin
-----------------------------------------------------

using it by executing the following commands in forge console(with linebreaks/whitespaces as below):
<pre>
new-project --named crud --topLevelPackage com.forge.crud --type war;
    
      
persistence setup --provider HIBERNATE --container JBOSS_AS7;
   
"n";
   
   
   
   
entity --named Person

   
crud setup
    
   
crud service-from-entity com.forge.crud.model.Person.java
   
   
</pre>

you should have the following classes generated:

...
...
```
Wrote /home/rmpestano/workspace/crud/src/main/java/com/forge/crud/Crud.java
[crud] Person.java $ 
[crud] Person.java $ crud service-from-entity com.forge.crud.model.Person.java
 ? An action has requested to install the following facets into your project [interface org.jboss.forge.spec.javaee.EJBFacet] continue? [Y/n] 
***SUCCESS*** Installed [forge.spec.ejb] successfully.
Wrote /home/rmpestano/workspace/crud/pom.xml
Wrote /home/rmpestano/workspace/crud/src/main/java/com/forge/service/PersonService.java
```

where PersonService is a hibernate based service ready to CRUD Person entity:
```java
package com.forge.service;

import com.forge.crud.Crud;
import com.manager.model.Person;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import java.util.List;

@Stateless
public class PersonService {

    @Inject
    protected Crud<Person> personCrud;

    public Person store(Person entity) {
        personCrud.saveOrUpdate(entity);
        return entity;
    }

    public void remove(Person entity) {
        personCrud.delete(entity);
    }

    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public Person find(Person entity) {
        return personCrud.example(entity).find();
    }

    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public List<Person> list(Person entity) {
        return personCrud.example(entity).list();
    }

    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public Crud crud() {
        return personCrud;
    }
}
```

testing the service with Arquillian is simple as below:

```java

import com.forge.service.PersonService;
import com.manager.model.Person;
import com.manager.model.Phone;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;

import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(Arquillian.class)
public class PersonServiceTest {

    @Inject
    PersonService personService;

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive archive = ShrinkWrap.create(WebArchive.class)
                .addPackages(true, "com.forge.crud")
                .addClass(PersonService.class)
                .addClass(Person.class)
                .addClass(Phone.class);
        archive.addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
        archive.addAsWebInfResource("web.xml", "web.xml");
        archive.addAsResource("META-INF/persistence.xml", "META-INF/persistence.xml");
        System.out.println(archive.toString(true));
        return archive;
    }


    @Test
    public void shouldInsertPerson(){
        Person p = new Person();
        p.setName("pestano");
        personService.store(p);
        assertTrue(p != null);
        assertTrue(p.getId() != null);
    }

    @Test
    public void shouldRemovePerson(){
        Person p = new Person();
        p.setName("pestano");
        Person personToRemove = personService.find(p);
        assertNotNull(personToRemove);
        personService.remove(personToRemove);
        Person removedPerson = personService.find(p);
        assertNull(removedPerson);
    }

}
```
