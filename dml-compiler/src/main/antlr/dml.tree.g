header {
    package pt.ist.fenixframework.dml;
}


{
import java.util.*;
import java.net.URL;
}
class DmlTreeParser extends TreeParser;

options {
	importVocab = Dml;
    defaultErrorHandler = false;
}



domainDefinitions[DomainModel model,URL sourceFile]
{
    String packageName = "";
}
    : #(DOMAIN_DEFS ( packageName=domainDefinition[model,packageName,sourceFile] )* )
    ;

domainDefinition[DomainModel model,String packageName,URL sourceFile] returns [String newPackageName = packageName]
{ 
    String name = null;
    String sc;
    String alias = null;
    List ifs;
    ValueType valueType = null;
}

    : #(CLASS_DEF 
            name=entityTypeIdentifier[packageName]
            sc=superClassClause[packageName] 
            ifs=implementsClause[packageName] 
            { 
                DomainEntity superclass = null;
                if (sc != null) {
                    superclass = model.findClassOrExternal(sc);
                    if (superclass == null) {
                        throw new SemanticException("Missing superclass: " + sc);
                    }
                }
                DomainClass domClass = new DomainClass(sourceFile, name, superclass, ifs); 
            }
            classBlock[model, domClass]
            { 
                model.addClass(domClass); 
            }
        )

    | #(EXTERNAL
            name=entityTypeIdentifier[packageName]
            ( alias=identifier )?
            { model.addExternalEntity(sourceFile, name, alias); }
        )

    | #(ENUM_TYPE
            name=identifier
            ( alias=identifier )?
            { model.newEnumType(alias, name); }
        )

    | #(VALUE_TYPE
            valueType=typeSpec[model, true]
            ( alias=identifier )?
            { model.newValueType(alias, valueType); }
            valueTypeBlock[model,valueType]
        )

    | #(RELATION_DEF
            name=identifier 
            //sc=superClassClause 
            //ifs=implementsClause 
            { 
                //DomainRelation superrelation = null;
                //if (sc != null) {
                //    superrelation = model.findRelation(sc);
                //    if (superrelation == null) {
                //        throw new SemanticException("Missing superrelation: " + sc);
                //    }
                //}
                //DomainRelation domRelation = new DomainRelation(name, superrelation, ifs); 
                DomainRelation domRelation = new DomainRelation(sourceFile, name, null, null); 
            }
            relationBlock[model,domRelation, packageName]
            { 
                model.addRelation(domRelation);
            }
        )

    | #(PACKAGE
            ( name=identifier )?
            { newPackageName = (name == null ? "" : name); }
        )
    ;

superClassClause[String packageName] returns [String superclass = null]
{ String id; }
	: #(EXTENDS_CLAUSE ( id=entityTypeIdentifier[packageName] { superclass = id; } )? )
	;

implementsClause[String packageName] returns [List superInterfaces = null]
{ String oneInterface; }
	: #(IMPLEMENTS_CLAUSE
            ( oneInterface=entityTypeIdentifier[packageName]
                { 
                    if (superInterfaces == null) {
                        superInterfaces = new ArrayList();
                    }
                  superInterfaces.add(oneInterface);
                } 
            )*
        )
    ;


classBlock[DomainModel model, DomainClass domClass]
{ Slot slotDef; }
	: #(OBJBLOCK
            ( slotDef=classSlot[model] { domClass.addSlot(slotDef); } )*
        )
	;


classSlot[DomainModel model] returns [Slot slotDef = null]
{
    ValueType slotType;
}
    : #(SLOT_DEF 
            slotType=typeSpec[model, false]
            name:IDENT
            { 
                slotDef = new Slot(name.getText(), slotType);
            }
            slotOptions[slotDef]
            annotations[slotDef]
        )
    ;


annotations[Slot slotDef]
    : #(ANNOTATIONS
         (
            name:ANN_NAME 
            {
                slotDef.addAnnotation(new Annotation(name.getText())); 
            }
         )*
       )
    ;


slotOptions[Slot slotDef]
    : #(SLOT_OPTIONS ( slotOption[slotDef] )*)
    ;


slotOption[Slot slotDef]
    : #(REQUIRED_OPTION { slotDef.addOption(Slot.Option.REQUIRED); } )
    ;

valueTypeBlock[DomainModel model, ValueType valueType]
    : #(VALUE_TYPE_BLOCK 
            externalizationClause[model,valueType] 
            ( internalizationClause[model,valueType] )?
        )
    ;

externalizationClause[DomainModel model, ValueType valueType]
    : #(EXTERNALIZATION_CLAUSE
            ( externalizationElement[model,valueType] )*
        )
    ;


externalizationElement[DomainModel model, ValueType valueType]
{
    String methodName;
    ValueType elemType;
}
    : #(EXTERNALIZATION_ELEMENT
            elemType=typeSpec[model, false]
            methodName=identifier
            {
                ExternalizationElement elem = new ExternalizationElement(elemType, methodName);
                valueType.getBaseType().addExternalizationElement(elem);
            }
        )
    ;

internalizationClause[DomainModel model, ValueType valueType]
{
    String methodName;
}
    : #(INTERNALIZATION_CLAUSE
            methodName=identifier
            { valueType.getBaseType().setInternalizationMethodName(methodName); }
        )
    ;

relationBlock[DomainModel model, DomainRelation domRelation, String packageName]
	: #(RELATION_BLOCK relationRolesAndSlots[model,domRelation,packageName] )
	;

relationRolesAndSlots[DomainModel model, DomainRelation domRelation, String packageName]
{ 
    Role roleDef; 
    Slot slotDef;
}
    : ( 
            roleDef=role[model,packageName] 
            { domRelation.addRole(roleDef); } 
        |   slotDef=classSlot[model]
            { domRelation.addSlot(slotDef); }
        )*  
    ;


role[DomainModel model, String packageName] returns [Role roleDef = null]
{
    String typeName;
    String roleName;
}
    : #(ROLE 
            typeName=entityTypeIdentifier[packageName]
            roleName=roleName
            {
                DomainEntity type = model.findClassOrExternal(typeName);
                if (type == null) {
                    throw new SemanticException("Unknown role type: " + typeName);
                }
                roleDef = new Role(roleName, type);
            }
            roleOptions[roleDef]
        )
    ;


roleName returns [String name = null]
    : #(ROLE_NAME ( id:IDENT { name = id.getText(); })? )
    ;

roleOptions[Role roleDef]
    : #(ROLE_OPTIONS ( roleOption[roleDef] )*)
    ;


roleOption[Role roleDef]
{
    int lower, upper, card;
}
    : #(MULTIPLICITY 
            (   #(MULTIPLICITY_RANGE 
                    lower=multiplicityBound
                    upper=multiplicityBound
                    { roleDef.setMultiplicity(lower, upper); }
                )
            |   upper=multiplicityBound
                { roleDef.setMultiplicity(0, upper); }
            )
        )
    | #(INDEXED name:IDENT card=indexCard { roleDef.setIndexProperty(name.getText()); roleDef.setIndexCardinality(card); } )
    | #(ORDERED { roleDef.setOrdered(true); } )
    ;

indexCard returns [int bound = 1]
    :
    	( STAR
        { bound = Role.MULTIPLICITY_MANY; })?
    ;


multiplicityBound returns [int bound = 0]
    :
        n:INT_NUMBER
        { bound = Integer.parseInt(n.getText()); }
    |   STAR
        { bound = Role.MULTIPLICITY_MANY; }
    ;


identifier returns [String name = ""]
{ String n1; }

    : id:IDENT
        { name = id.getText(); }
    | #(DOT n1=identifier n2:IDENT)
        { name = n1 + "." + n2.getText(); }
    ;

entityTypeIdentifier[String packageName] returns [String name = ""]
{ String n1; }

    :   n1=identifier
        { 
            if ((packageName == null) || packageName.equals("")) {
                name = n1;
            } else {
                name = packageName + "." + n1;
            }
        }
    |   #(ABSOLUTE_NAME n1=identifier)
        // this is an absolute entityType name, so do not use the packageName
        // and simply return the name without the leading dot
        { name = n1; }
    ;


typeSpec[DomainModel model,boolean create] returns [ValueType vt = null]
{ String baseTypeName, typeArgs; }

    :   #(TYPE
            baseTypeName=identifier
            { 
                if (create) {
                    vt = new PlainValueType(baseTypeName);
                } else {
                    vt = model.findValueType(baseTypeName);
                    if (vt == null) {
                        throw new SemanticException("Unknown value type: " + baseTypeName);
                    }
                }
            }
            ( typeArgs=typeArguments { vt = new ParamValueType(vt.getBaseType(), "<" + typeArgs + ">"); } )?
         )
    ;

typeArguments returns [String args = ""]
{ String arg1, arg2; }

    :   arg1=typeArgument
        { args = arg1; }
    |   #(COMMA arg1=typeArguments arg2=typeArgument)
        { args = arg1 + "," + arg2; }
    ;


typeArgument returns [String arg = ""]
    :   arg=typeSpecAsString
    |   arg=wildcard
    ;

wildcard returns [String arg = "?"]
{ String bounds = ""; }
    :   #(WILDCARD ( bounds=wildcardBounds { arg = "?" + bounds; } )? )
    ;

wildcardBounds returns [String bounds = ""]
{ String type; }
    :   #(WILDCARD_EXTENDS type=typeSpecAsString)
        { bounds = " extends " + type; }
    |   #(WILDCARD_SUPER type=typeSpecAsString)
        { bounds = " super " + type; }
    ;

typeSpecAsString returns [String type = ""]
{ String baseTypeName, typeArgs; }

    :   #(TYPE
            baseTypeName=identifier
            { type = baseTypeName; }
            ( typeArgs=typeArguments { type += "<" + typeArgs + ">"; } )?
        )
    ;
