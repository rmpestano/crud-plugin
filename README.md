A simple [JBoss Forge](forge.jboss.org/) crud generator plugin based on Hibernate API.
-----------------------------------------------------

using it by executing the following commands in forge console(with linebreaks/whitespaces as below):
<pre>
new-project --named crud --topLevelPackage com.forge.crud --type war;
    
      
persistence setup --provider HIBERNATE --container JBOSS_AS7;
   
"n";
   
   
   
   
entity --named Person;

  
   
field string --named name;
   

beans setup;
  

forge install-plugin crud 
  
crud setup;
    
   
crud service-from-entity --entity com.forge.crud.model.Person.java;
   
forge install-plugin arquillian 
  
arquillian setup --containerName JBOSS_AS_MANAGED_7
  
  
  
"y";

arquillian create-test --class com.forge.crud.service.PersonService.java;
  
    
</pre>

you should have the following classes generated:

...
...
```
Wrote /home/rmpestano/workspace/crud/src/main/java/com/forge/crud/Crud.java
[crud] Person.java $ 
[crud] Person.java $ crud service-from-entity --entity com.forge.crud.model.Person.java
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

Crud is based on fluent api(builder patter) so for example imagine you want to list Persons
which contains a name and some of the given phone numbers:
```java

 public class AService{
    @Inject
    Crud<Person> crud;
 
    public List<Person> findByNameAndPhones(String name, List<Phone> phones) {
         //if crud criteria is null it will create one on demand(use crud.resetCruteria to guarantee it is clean)
         crud.resetCriteria();    
         return crud.ilike("name", name, MatchMode.ANYWHERE)//nullsafe (will add ilike only if name is not null)
                 .join("phone", "phone").//a person can have one or more telephones 
                 .in("phone.numbers", phones)     
                 .and(Restrictions.eq("aProperty","aValue"))//can use conjunstions and disjunctions(all nullsafe)
                 .addCriterion(Restrictions.or(Restrictions.eq("aProperty","aValue")))//can add criterion(also nullsafe 
                 .isNotEmpty("name")
                 .list();//criteria is cleared after its execution
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
                .addClass(Person.class);
        archive.addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
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

   
see it in action:

[![ScreenShot](https://raw.github.com/GabLeRoux/WebMole/master/ressources/WebMole_Youtube_Video.png)](http://youtu.be/UZiQrjRpLW4)

