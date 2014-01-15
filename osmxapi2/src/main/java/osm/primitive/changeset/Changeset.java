package osm.primitive.changeset;

import java.util.List;

import osm.primitive.Primitive;
import osm.primitive.PrimitiveTypeEnum;
import osm.primitive.Tag;

public class Changeset extends Primitive {

    @Override
    public PrimitiveTypeEnum getType() {
        return PrimitiveTypeEnum.changeset;
    }

    
}
