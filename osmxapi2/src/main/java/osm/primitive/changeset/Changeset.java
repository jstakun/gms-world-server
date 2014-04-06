package osm.primitive.changeset;

import osm.primitive.Primitive;
import osm.primitive.PrimitiveTypeEnum;

public class Changeset extends Primitive {

    @Override
    public PrimitiveTypeEnum getType() {
        return PrimitiveTypeEnum.changeset;
    }

    
}
