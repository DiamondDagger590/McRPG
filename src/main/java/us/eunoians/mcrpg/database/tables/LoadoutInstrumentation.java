package us.eunoians.mcrpg.database.tables;

import com.cyr1en.flatdb.annotations.Column;
import com.cyr1en.flatdb.annotations.Table;
import javassist.ClassPool;
import javassist.LoaderClassPath;
import javassist.bytecode.AccessFlag;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.FieldInfo;
import javassist.bytecode.annotation.Annotation;
import us.eunoians.mcrpg.McRPG;

import java.security.ProtectionDomain;

public class LoadoutInstrumentation {

  private ClassPool classPool;
  private ClassLoader cl;
  private ProtectionDomain pd;
  private int loadOutSize;

  public LoadoutInstrumentation(McRPG instance, int loadOutSize) {
    this.loadOutSize = loadOutSize;
    classPool  = ClassPool.getDefault();
    cl = instance.getClass().getClassLoader();
    pd = instance.getClass().getProtectionDomain();
    classPool.appendClassPath(new LoaderClassPath(cl));
    classPool.appendClassPath(new LoaderClassPath(Table.class.getClassLoader()));
    classPool.appendClassPath(new LoaderClassPath(Column.class.getClassLoader()));
  }

  private ClassFile addFields() throws Exception {
    ClassFile cf = classPool.get(Loadout.class.getName()).getClassFile();

    AnnotationsAttribute annotationsAttribute = new AnnotationsAttribute(cf.getConstPool(), AnnotationsAttribute.visibleTag);
    Annotation annotation = new Annotation("com.cyr1en.flatdb.annotations.Column", cf.getConstPool());
    annotationsAttribute.addAnnotation(annotation);

    for (int i = 1; i <= loadOutSize; i++) {
      FieldInfo fieldInfo = new FieldInfo(cf.getConstPool(), "slot" + i, "Ljava/lang/String;");
      fieldInfo.setAccessFlags(AccessFlag.PUBLIC);
      fieldInfo.addAttribute(annotationsAttribute);
      cf.addField(fieldInfo);
    }
    return cf;
  }

  public Class<?> instrument() {
    try {
      ClassFile cf = addFields();
      cf.setName("ILoadoutTable");
      return classPool.makeClass(cf).toClass(cl, pd);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }
}
