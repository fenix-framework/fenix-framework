header {
    package pt.ist.fenixframework.dml;
}

/** DML parser
 *
 * Many bits are stolen from the java.g grammar that came with 
 * ANTLR 2.7.2 in the examples/java/java directory.
 *
 */


class DmlParser extends Parser;

options {
	exportVocab=Dml;
    buildAST = true;
    k = 2;
    defaultErrorHandler = false;
//    analyzerDebug = true;
}

tokens {
    DOMAIN_DEFS; CLASS_DEF; EXTENDS_CLAUSE; OBJBLOCK; IMPLEMENTS_CLAUSE;
    RELATION_DEF; SLOT_DEF; RELATION_BLOCK; ROLE; ANNOTATIONS;
    ROLE_NAME; ROLE_OPTIONS; MULTIPLICITY; MULTIPLICITY_RANGE; EXTERNAL;
    INDEXED; ORDERED; VALUE_TYPE; SLOT_OPTIONS; REQUIRED_OPTION;
    ENUM_TYPE; PACKAGE; ABSOLUTE_NAME; VALUE_TYPE_BLOCK; EXTERNALIZATION_CLAUSE;
    EXTERNALIZATION_ELEMENT; INTERNALIZATION_CLAUSE; TYPE;
    WILDCARD; WILDCARD_EXTENDS; WILDCARD_SUPER;
}


domainDefinitions
    :   ( domainDefinition )* 
        EOF!
		{#domainDefinitions = #([DOMAIN_DEFS, "DOMAIN_DEFS"], #domainDefinitions);}
    ;

domainDefinition
	:   ( classDefinition
		| relationDefinition
        | externalDeclaration
        | valueTypeDeclaration
        | packageDeclaration
		)
    ;

externalDeclaration!
    :   "external" "class" id:entityTypeIdentifier alias:aliasIdentifier SEMI!
        {#externalDeclaration = #(#[EXTERNAL,"EXTERNAL"], id, alias);}
    ;


valueTypeDeclaration
    :   ( enumTypeDeclaration | compositeValueTypeDeclaration )
    ;

enumTypeDeclaration!
    :   "enum" id:identifier alias:aliasIdentifier SEMI!
        {#enumTypeDeclaration = #(#[ENUM_TYPE,"ENUM_TYPE"], id, alias);}
    ;

compositeValueTypeDeclaration!
    :   "valueType" t:typeSpec alias:aliasIdentifier block:valueTypeBlock
        {#compositeValueTypeDeclaration = #(#[VALUE_TYPE,"VALUE_TYPE"], t, alias, block);}
    ;

aliasIdentifier
    :
        ( "as"! identifier )?
    ;

valueTypeBlock!
	:	LCURLY! 
        ec:externalizationClause 
        ( ic:internalizationClause )?
        RCURLY!
		{#valueTypeBlock = #([VALUE_TYPE_BLOCK, "VALUE_TYPE_BLOCK"], ec, ic);}
	;

externalizationClause!
    :   "externalizeWith" LCURLY elems:externalizationElements RCURLY
        {#externalizationClause = #([EXTERNALIZATION_CLAUSE,"EXTERNALIZATION_CLAUSE"], elems);}
    ;

externalizationElements
    :   externalizationElement ( externalizationElement )*
    ;

externalizationElement!
    :   t:typeSpec id:identifier LPAREN! RPAREN! SEMI!
        {#externalizationElement = #(#[EXTERNALIZATION_ELEMENT,"EXTERNALIZATION_ELEMENT"], t, id);}
    ;

internalizationClause!
    :   "internalizeWith" id:identifier LPAREN! RPAREN! SEMI!
        {#internalizationClause = #(#[INTERNALIZATION_CLAUSE,"INTERNALIZATION_CLAUSE"], id);}
    ;

packageDeclaration!
    :   "package" ( id:identifier )? SEMI!
        {#packageDeclaration = #(#[PACKAGE,"PACKAGE"], id);}
    ;

classDefinition!
	:	"class" 
        id:entityTypeIdentifier
		// it _might_ have a superclass...
		sc:superClassClause
		// it might implement some interfaces...
		ic:implementsClause
		// now parse the body of the class
		cb:classBlock
		{#classDefinition = #(#[CLASS_DEF,"CLASS_DEF"], id, sc, ic, cb);}
	;

superClassClause!
	:	( "extends" id:entityTypeIdentifier )?
		{#superClassClause = #(#[EXTENDS_CLAUSE,"EXTENDS_CLAUSE"], id);}
	;

// A class can implement several interfaces...
implementsClause
	:	(
			i:"implements"! entityTypeIdentifier ( COMMA! entityTypeIdentifier )*
		)?
		{#implementsClause = #(#[IMPLEMENTS_CLAUSE,"IMPLEMENTS_CLAUSE"],
								 #implementsClause);}
    ;

classBlock
	:	( ( LCURLY! 
                ( classSlot )* 
            RCURLY!
          )
        |
            SEMI!
        )
		{#classBlock = #([OBJBLOCK, "OBJBLOCK"], #classBlock);}
	;


classSlot
    :
    	ann:annotations!
    	t:typeSpec!
        classSlotInternal[#t, #ann]
    ;


protected classSlotInternal![AST type, AST ann]
    :
		id:IDENT
        so:slotOptions
        SEMI!
		{#classSlotInternal = #(#[SLOT_DEF,"SLOT_DEF"], type, id, so, ann);}
    ;
    
    
annotations
    : (
        name:ANN_NAME
      )*
        {#annotations = #([ANNOTATIONS, "ANNOTATIONS"], #annotations);}
    ;


slotOptions
    : ( LPAREN! 
         slotOption
        RPAREN!
      )?
      {#slotOptions = #([SLOT_OPTIONS, "SLOT_OPTIONS"], #slotOptions);}
    ;


slotOption!
    :
        "REQUIRED"
        {#slotOption = #([REQUIRED_OPTION,"REQUIRED_OPTION"]);}
    ;


typeSpec!
    :   id:identifier ( ta:typeArguments )?
        {#typeSpec = #([TYPE,"TYPE"], id, ta);}
    ;

typeArguments
    :   LANGLE! typeArgument ( COMMA^ typeArgument )* RANGLE!
    ;

typeArgument
    :   typeSpec
    |   wildcard
    ;

wildcard!
    :   QUESTION ( wb:wildcardBounds )?
        {#wildcard = #([WILDCARD,"WILDCARD"], wb);}
    ;

wildcardBounds!
    :   "extends" t1:typeSpec
        {#wildcardBounds = #([WILDCARD_EXTENDS,"WILDCARD_EXTENDS"], t1);}
    |   "super" t2:typeSpec
        {#wildcardBounds = #([WILDCARD_SUPER,"WILDCARD_SUPER"], t2);}
    ;


relationDefinition!
	:	"relation" 
        id:identifier
		// it _might_ have a superclass...
		//sc:superClassClause
		// it might implement some interfaces...
		//ic:implementsClause
		// now parse the body of the relation
		rb:relationBlock
		//{#relationDefinition = #(#[RELATION_DEF,"RELATION_DEF"], id, sc, ic, rb);}
		{#relationDefinition = #(#[RELATION_DEF,"RELATION_DEF"], id, rb);}
	;



relationBlock!
	:
        LCURLY!
        rs:rolesAndSlots
        RCURLY!
		{#relationBlock = #([RELATION_BLOCK, "RELATION_BLOCK"], rs);}
	;


rolesAndSlots
    :
        (
            t:entityTypeIdentifier!
            // HACK! classSlotInternal should not have an entityTypeIdentifier
            // but since these are not currently used...
            ( role[#t] | classSlotInternal[#t, null] )
        )*
    ;

role![AST type]
    :
        "playsRole"
        rn:roleName
        ro:roleOptions
        {#role = #([ROLE, "ROLE"], type, rn, ro);}
    ;

roleName
    :
        ( IDENT )?
        {#roleName = #([ROLE_NAME, "ROLE_NAME"], #roleName);}
    ;

roleOptions
    : ( ( LCURLY!
          ( roleOption SEMI! )*
          RCURLY!
        )
        |
        SEMI!
      )
      {#roleOptions = #([ROLE_OPTIONS, "ROLE_OPTIONS"], #roleOptions);}
    ;

roleOption!
    :
        "multiplicity" range:multiplicityRange
        {#roleOption = #([MULTIPLICITY, "MULTIPLICITY"], range);}
    |   "indexed" "by" ip:identifier ( CARDINAL LPAREN ( card:multiplicityUpperOnly )? RPAREN )? 
        {#roleOption = #([INDEXED,"INDEXED"], ip, card);}
    |   "ordered"
        {#roleOption = #([ORDERED,"ORDERED"]);}
    ;

multiplicityRange!
    :
        lower:INT_NUMBER MULT_RANGE upper:multiplicityUpperOnly
        {#multiplicityRange = #([MULTIPLICITY_RANGE, "MULTIPLICITY_RANGE"], lower, upper);}
    |   upperOnly:multiplicityUpperOnly
        {#multiplicityRange = #upperOnly;}
    ;

multiplicityUpperOnly
    :
        INT_NUMBER
    |   STAR
    ;


// A (possibly-qualified) java identifier.  We start with the first IDENT
//   and expand its name by adding dots and following IDENTS
identifier 
    :   IDENT  ( DOT^ IDENT )*
    ;

// With the addition of packages, names of entities may start with a dot
entityTypeIdentifier!
    :   id1:identifier
        {#entityTypeIdentifier = #id1;}
    |   DOT id2:identifier
        {#entityTypeIdentifier = #([ABSOLUTE_NAME,"ABSOLUTE_NAME"], id2);}
    ;



class DmlLexer extends Lexer;
options {
    k=2;                   // two characters of lookahead
}


ANN_NAME
    :  ('{') ('"') ('a'..'z'|'A'..'Z') ('a'..'z'|'A'..'Z')* ('"') (':') ('a'..'z'|'A'..'Z') ('a'..'z'|'A'..'Z')* ('}')
    ;


IDENT
    :   ('a'..'z'|'A'..'Z'|'_'|'$') ('a'..'z'|'A'..'Z'|'_'|'0'..'9'|'$')*
    ;


INT_NUMBER 
    : ('0'..'9')+
    ;


DOT             :   '.'     ;
LPAREN          :   '('     ;
RPAREN          :   ')'     ;
COLON           :   ':'     ;
COMMA           :   ','     ;
LCURLY          :   '{'     ;
RCURLY          :   '}'     ;
SEMI			:	';'		;
STAR            :   '*'     ;
MULT_RANGE      :   ".."    ;
LANGLE          :   '<'     ;
RANGLE          :   '>'     ;
QUESTION        :   '?'     ;
CARDINAL        :   '#'     ;


// Whitespace -- ignored
WS  :   (   ' '
        |   '\t'
        |   '\f'
        // handle newlines
        |   (   "\r\n"  // Evil DOS
            |   '\r'    // Macintosh
            |   '\n'    // Unix (the right way)
            )
            { newline(); }
        )
        { _ttype = Token.SKIP; }
    ;

// Single-line comments
SL_COMMENT
    :   "//"
        (~('\n'|'\r'))* ('\n'|'\r'('\n')?)
        {$setType(Token.SKIP); newline();}
    ;

// multiple-line comments
ML_COMMENT
    :   "/*"
        (   /*  '\r' '\n' can be matched in one alternative or by matching
                '\r' in one iteration and '\n' in another.  I am trying to
                handle any flavor of newline that comes in, but the language
                that allows both "\r\n" and "\r" and "\n" to all be valid
                newline is ambiguous.  Consequently, the resulting grammar
                must be ambiguous.  I'm shutting this warning off.
             */
            options {
                generateAmbigWarnings=false;
            }
        :
            { LA(2)!='/' }? '*'
        |   '\r' '\n'       {newline();}
        |   '\r'            {newline();}
        |   '\n'            {newline();}
        |   ~('*'|'\n'|'\r')
        )*
        "*/"
        {$setType(Token.SKIP);}
    ;


// string literals
STRING_LITERAL
	:	'"' (~('"'))* '"'
	;


// a dummy rule to force vocabulary to be all characters (except special
//   ones that ANTLR uses internally (0 to 2)
protected
VOCAB
	:	'\3'..'\377'
	;


