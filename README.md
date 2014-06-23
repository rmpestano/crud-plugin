[JBoss Forge](forge.jboss.org/) crud generator plugin
-----------------------------------------------------

using it, execute the following commands in forge(with linebreaks as below):
```
new-project --named crud --topLevelPackage com.forge.crud --type war;


persistence setup --provider HIBERNATE --container JBOSS_AS7;

"n";




entity --named Person

crud setup


crud service-from-entity com.forge.crud.model.Person.java

```

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

