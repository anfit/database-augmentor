package pl.edu.mimuw.dbaugmentor.copier;

import pl.edu.mimuw.dbaugmentor.config.ApplicationProperties;
import pl.edu.mimuw.dbaugmentor.database.*;

import java.util.*;
import java.util.logging.Logger;

public class EntityCopier {
    private final Database database;
    private final Set<EntityCopy> copiedEntitiesToOrder = new HashSet<>();
    private final ArrayList<Entity> entitiesToInsert = new ArrayList<>();
    private final ArrayList<Update> updatesOnCopiedEntities = new ArrayList<>();
    private final ApplicationProperties properties;

    public ArrayList<Update> getUpdatesOnCopiedEntities() {
        return updatesOnCopiedEntities;
    }

    public EntityCopier(Database database, ApplicationProperties properties) {
        this.database = database;
        this.properties = properties;
    }

    public ArrayList<Entity> getEntitiesToInsert() {
        return this.entitiesToInsert;
    }

    public void copyEntities(Logger logger) throws Exception {
        if (properties.getMultiplier() < 2) {
            throw new Exception("Multiplier has to be >= 2");
        }
        Table table = database.getTable(properties.getTableName());
        Collection<Entity> entitiesToCopy = table.getEntities();
        Collection<Entity> newEntities = new ArrayList<>();
        for (int copyNumber = 1; copyNumber < properties.getMultiplier(); copyNumber++) {
            int toCopy = entitiesToCopy.size();
            int copied = 0;
            for (Entity entityToCopy : entitiesToCopy) {
                newEntities.add(createEntityCopy(entityToCopy, copyNumber).getEntity());
                copied++;
                logger.info("copied " + copied + " / " + toCopy + " rows from " + table.getName());
            }
            Collection<Entity> tmp = entitiesToCopy;
            entitiesToCopy = newEntities;
            newEntities = tmp;
            newEntities.clear();
        }
        if (properties.isCaching()) {
            fixUniqueKeyValues();
        }
        if (properties.isCaching() && !properties.isIgnoringFkConstraints()) {
            orderEntities();
        }
        else {
            for (EntityCopy entityCopy : copiedEntitiesToOrder) {
                entitiesToInsert.add(entityCopy.getEntity());
            }
        }
    }

    private EntityCopy createEntityCopy(Entity entityToCopy, int copyNumber) throws Exception {
        EntityCopy newEntity = new EntityCopy(entityToCopy, copyNumber);
        fixPrimaryKeyValue(newEntity.getEntity());
        copiedEntitiesToOrder.add(newEntity);
        for (IncomingReference incomingReference: entityToCopy.getIncomingReferences()) {
            Entity childEntityToCopy = incomingReference.getEntity();
            EntityCopy newChildEntity;
            if (!childEntityToCopy.isCopied()) {
                newChildEntity = createEntityCopy(childEntityToCopy, copyNumber);
            } else {
                newChildEntity = childEntityToCopy.getEntityCopy();
            }
            ForeignKeyDefinition fkDefinition = incomingReference.getFkDefinition();
            IncomingReference newIncomingReference = new IncomingReference(newChildEntity.getEntity(), fkDefinition);
            newIncomingReference.setFKValueFromDestinationEntity(newEntity.getEntity());
            newEntity.getEntity().addIncomingReference(newIncomingReference);
            newEntity.addDependent(newChildEntity, fkDefinition);
            newChildEntity.addDependency(newEntity, fkDefinition);
        }
        return newEntity;
    }

    private void fixPrimaryKeyValue(Entity entity) throws Exception {
        entity.fixUniqueKeyValue(
                entity.getTable().getPrimaryKey(),
                properties.isGeneratingReadableString(),
                properties.isCachingUniqueKey());
    }

    private void fixUniqueKeyValues() throws Exception {
        for (EntityCopy entityCopy : copiedEntitiesToOrder) {
            for (UniqueKey uniqueKey: entityCopy.getEntity().getTable().getUniqueKeys()) {
                entityCopy.getEntity().fixUniqueKeyValue(
                        uniqueKey,
                        properties.isGeneratingReadableString(),
                        properties.isCachingUniqueKey());
            }
        }
    }

    private void orderEntities() throws Exception {
        Queue<EntityCopy> nonDependent = new LinkedList<>();
        for (EntityCopy entityCopy : copiedEntitiesToOrder) {
            if (entityCopy.hasNoDependencies()) {
                nonDependent.add(entityCopy);
            }
        }
        while (!nonDependent.isEmpty()) {
            EntityCopy entityCopy = nonDependent.remove();
            entitiesToInsert.add(entityCopy.getEntity());
            copiedEntitiesToOrder.remove(entityCopy);
            for (InsertDependency dependent : entityCopy.getDependent()) {
                if (dependent.getEntityCopy().removeDependency(entityCopy)) {
                    if (dependent.getEntityCopy().hasNoDependencies()) {
                        nonDependent.add(dependent.getEntityCopy());
                    }
                }
            }
            if (nonDependent.isEmpty() && !copiedEntitiesToOrder.isEmpty()) {
                for (EntityCopy next : copiedEntitiesToOrder) {
                    if (next.allDependenciesNullable()) {
                        updatesOnCopiedEntities.add(next.getUpdateOnNullableFkFields());
                        next.nullDependencies();
                        next.clearDependencies();
                        nonDependent.add(next);
                    }
                }
            }
        }

        if (!copiedEntitiesToOrder.isEmpty()) {
            throw new Exception("Unable to order entities, try \"Ignore foreign key constraints \" option.");
        }
    }
}
