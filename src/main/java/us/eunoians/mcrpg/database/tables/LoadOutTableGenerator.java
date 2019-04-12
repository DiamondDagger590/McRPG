package us.eunoians.mcrpg.database.tables;

import com.cyr1en.flatdb.annotations.Table;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.ConstPool;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.BooleanMemberValue;
import javassist.bytecode.annotation.StringMemberValue;
import lombok.Getter;

@Table(nameOverride = "loadout")
public class LoadOutTableGenerator {

  private CtClass tableCtClass;
  private int loadOutSize;

  public LoadOutTableGenerator(int loadOutSize) {
    this.loadOutSize = loadOutSize;
  }

  private void generate() throws Exception {
    ClassPool classPool = ClassPool.getDefault();
    tableCtClass = classPool.makeClass("us.eunoians.mcrpg.database.tables.LoadOutTable");

    tableCtClass.addField(CtField.make(FieldData.of("private", "String", "id").get(), tableCtClass));
    tableCtClass.addField(CtField.make(FieldData.of("private", "String", "uuid").get(), tableCtClass));

    addAnnotationToClass(tableCtClass, "com.cyr1en.flatdb.annotations.Table", "loadout");
    addAnnotationToField(tableCtClass, "id", "com.cyr1en.flatdb.annotations.Column", "autoIncrement", true);
    addAnnotationToField(tableCtClass, "uuid", "com.cyr1en.flatdb.annotations.Column", "primaryKey", true);


    for (int i = 1; i <= loadOutSize; i++) {
      FieldData fieldData = FieldData.of("private", "String", "slot" + i);
      tableCtClass.addField(CtField.make(fieldData.get(), tableCtClass));
      addAnnotationToField(tableCtClass, fieldData.getFieldName(), "com.cyr1en.flatdb.annotations.Column");
    }
  }

  private void addAnnotationToClass(CtClass ctClass, String annotationName, String tableName) {
    ClassFile classFile = ctClass.getClassFile();
    ConstPool constpool = classFile.getConstPool();

    AnnotationsAttribute annotationsAttribute = new AnnotationsAttribute(constpool, AnnotationsAttribute.visibleTag);
    Annotation annotation = new Annotation(annotationName, constpool);
    annotation.addMemberValue("nameOverride", new StringMemberValue(tableName, constpool));
    annotationsAttribute.addAnnotation(annotation);
    ctClass.getClassFile().addAttribute(annotationsAttribute);
  }

  public void addAnnotationToField(CtClass clazz, String fieldName, String annotationName) throws Exception {
    addAnnotationToField(clazz, fieldName, annotationName, null, false);
  }

  public void addAnnotationToField(CtClass clazz, String fieldName, String annotationName, String memberValueName, boolean value) throws Exception {
    ClassFile classFile = clazz.getClassFile();
    ConstPool constpool = classFile.getConstPool();

    CtField classField = clazz.getField(fieldName);

    AnnotationsAttribute annotationsAttribute = new AnnotationsAttribute(constpool, AnnotationsAttribute.visibleTag);
    Annotation annotation = new Annotation(annotationName, constpool);
    if (memberValueName != null && value)
      annotation.addMemberValue(memberValueName, new BooleanMemberValue(true, constpool));
    annotationsAttribute.addAnnotation(annotation);

    classField.getFieldInfo().addAttribute(annotationsAttribute);
  }

  public Class<?> asClass() {
    try {
      generate();
      return tableCtClass.toClass();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  private static class FieldData {
    @Getter private String accessModifier;
    @Getter private String type;
    @Getter private String fieldName;

    public FieldData(String accessModifier, String type, String fieldName) {
      this.accessModifier = accessModifier;
      this.type = type;
      this.fieldName = fieldName;
    }

    public static FieldData of(String accessModifier, String type, String fieldName) {
      return new FieldData(accessModifier, type, fieldName);
    }

    public String get() {
      return toString();
    }

    public String toString() {
      return String.format("%s %s %s;", accessModifier, type, fieldName);
    }
  }
}
