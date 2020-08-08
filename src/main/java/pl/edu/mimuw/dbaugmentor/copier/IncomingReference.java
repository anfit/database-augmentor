package pl.edu.mimuw.dbaugmentor.copier;

import pl.edu.mimuw.dbaugmentor.database.ForeignKeyDefinition;

public class IncomingReference {
    private final Entity entity;
    private final ForeignKeyDefinition fkDefinition;

    public IncomingReference(Entity entity, ForeignKeyDefinition foreignKeyDefinition) {
        this.entity = entity;
        this.fkDefinition = foreignKeyDefinition;
    }

    public Entity getEntity() {
        return entity;
    }

    public ForeignKeyDefinition getFkDefinition() {
        return fkDefinition;
    }

    public void setFKValueFromDestinationEntity(Entity destinationEntity) {
        for (int i = 0; i < getFkDefinition().getDestinationColumns().size(); i++) {
            String destColumnName = getFkDefinition().getDestinationColumns().get(i).getName();
            String srcColumnName = getFkDefinition().getSourceColumns().get(i).getName();
            Object valueToSet = destinationEntity.getColumnValue(destColumnName);
            entity.setColumnValue(srcColumnName, valueToSet);
        }
    }
}
