package pl.edu.mimuw.dbaugmentor.copier;

import pl.edu.mimuw.dbaugmentor.database.ForeignKeyDefinition;

public class InsertDependency {
    private final EntityCopy entityCopy;
    private final ForeignKeyDefinition foreignKeyDefinition;

    public EntityCopy getEntityCopy() {
        return entityCopy;
    }

    public ForeignKeyDefinition getForeignKeyDefinition() {
        return foreignKeyDefinition;
    }

    public InsertDependency(EntityCopy entityCopy, ForeignKeyDefinition foreignKeyDefinition) {
        this.entityCopy = entityCopy;
        this.foreignKeyDefinition = foreignKeyDefinition;
    }
}
