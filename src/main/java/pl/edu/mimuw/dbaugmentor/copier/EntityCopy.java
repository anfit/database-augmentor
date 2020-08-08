package pl.edu.mimuw.dbaugmentor.copier;

import pl.edu.mimuw.dbaugmentor.database.Column;
import pl.edu.mimuw.dbaugmentor.database.ForeignKeyDefinition;
import pl.edu.mimuw.dbaugmentor.database.Update;

import java.util.ArrayList;
import java.util.Map;

public class EntityCopy {
    private final Entity entity;
    private final ArrayList<InsertDependency> dependent = new ArrayList<>();
    private final ArrayList<InsertDependency> dependencies = new ArrayList<>();

    public EntityCopy(Entity entityToCopy, int copyNumber) {
        entity = new Entity();
        this.entity.setTable(entityToCopy.getTable());
        for (Map.Entry<String, Object> entry: entityToCopy.getColumnToValue().entrySet()) {
            this.entity.setColumnValue(entry.getKey(), entry.getValue());
        }
        entity.setOriginal(entityToCopy.getOriginal());
        entityToCopy.setEntityCopy(this);
        this.entity.setCopyNumber(copyNumber);
    }

    public Entity getEntity() {
        return this.entity;
    }

    public void addDependency(EntityCopy entityCopy, ForeignKeyDefinition foreignKeyDefinition) {
        dependencies.add(new InsertDependency(entityCopy, foreignKeyDefinition));
    }

    public boolean removeDependency(EntityCopy entityCopy) {
        for (InsertDependency insertDependency : dependencies) {
            if (insertDependency.getEntityCopy() == entityCopy) {
                dependencies.remove(insertDependency);
                return true;
            }
        }
        return false;
    }

    public void clearDependencies() {
        dependencies.clear();
    }

    public boolean hasNoDependencies() {
        return dependencies.isEmpty();
    }

    public void addDependent(EntityCopy dependentCopy, ForeignKeyDefinition foreignKeyDefinition) {
        dependent.add(new InsertDependency(dependentCopy, foreignKeyDefinition));
    }

    public ArrayList<InsertDependency> getDependent() {
        return this.dependent;
    }

    public boolean allDependenciesNullable() {
        for (InsertDependency dependency : dependencies) {
            ForeignKeyDefinition fkDefinition = dependency.getForeignKeyDefinition();
            for (Column column : fkDefinition.getSourceColumns()) {
                if (!column.isNullable()) {
                    return false;
                }
            }
        }
        return true;
    }

    public Update getUpdateOnNullableFkFields() {
        Update update = new Update(entity.getTable(), entity.getIdType(), entity.getColumnValue("id"));
        for (InsertDependency dependency : dependencies) {
            ForeignKeyDefinition fkDefinition = dependency.getForeignKeyDefinition();
            for (Column column : fkDefinition.getSourceColumns()) {
                if (column.isNullable()) {
                    update.setColumnValue(column, entity.getColumnValue(column.getName()));
                }
            }
        }
        return update;
    }

    public void nullDependencies() throws Exception {
        for (InsertDependency dependency : dependencies) {
            ForeignKeyDefinition fkDefinition = dependency.getForeignKeyDefinition();
            for (Column column : fkDefinition.getSourceColumns()) {
                if (column.isNullable()) {
                    entity.setColumnValue(column.getName(), null);
                } else {
                    throw new Exception("allDependenciesNullable() must be true in order to run nullDependencies()");
                }
            }
        }
    }
}
