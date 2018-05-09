package com.example;

import com.google.auto.service.AutoService;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.Element;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

@AutoService(Processor.class)
public class BindViewProcessor extends AbstractProcessor {

    private Messager mMessager;
    private Elements mElementUtils;
    private Map<String, ClassCreatorProxy> mProxyMap = new HashMap<>();


    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        mMessager = processingEnv.getMessager();
        mElementUtils = processingEnv.getElementUtils();
    }

    /**
     * 设置需要遍历的注解类
     * 可以添加多个注解
     * <p>
     * 如果遍历的所有类中都没有使用到supportTypes里面的任何一个注解，那么将不会执行process方法
     *
     * @return
     */
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        HashSet<String> supportTypes = new LinkedHashSet<>();
        supportTypes.add(BindView.class.getName());

        return supportTypes;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        mMessager.printMessage(Diagnostic.Kind.NOTE, "processing...");
        mProxyMap.clear();

        //得到所有的注解
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(BindView.class);
        for (Element element : elements) {

            //拿到BindView 修饰的字段的元素  @BindView(R.id.a) TextView tx ，tx就是这个elements
            mMessager.printMessage(Diagnostic.Kind.NOTE, "elements..." + element.toString());

            VariableElement variableElement = (VariableElement) element;
            //拿到tx 元素的 类名， 也就是MainActivity的类名
            TypeElement typeElement = (TypeElement) variableElement.getEnclosingElement();
            //拿到 MainActivity 类名的全路径
            String fullClassName = typeElement.getQualifiedName().toString();


            mMessager.printMessage(Diagnostic.Kind.NOTE, "typeElement--" + typeElement.toString());

            ClassCreatorProxy proxy = mProxyMap.get(fullClassName);

            if (proxy == null) {
                proxy = new ClassCreatorProxy(mElementUtils, typeElement);
                mProxyMap.put(fullClassName, proxy);
            }
            //拿到tx的注解对象
            BindView bindAnnotation = variableElement.getAnnotation(BindView.class);
            //然后拿到id
            int id = bindAnnotation.value();
            //将id于对应的类存储到Map集合中
            proxy.putElement(id, variableElement);

            //获取应用的包名
            PackageElement packageElement = mElementUtils.getPackageOf(typeElement);
            //packageName
            String packageName = packageElement.getQualifiedName().toString();
            mMessager.printMessage(Diagnostic.Kind.NOTE, "packageName..." + packageName);
            String className = typeElement.getSimpleName().toString();
            mMessager.printMessage(Diagnostic.Kind.NOTE, "className..." + className);

            //拿到TextView的全路径
            String type = variableElement.asType().toString();

            mMessager.printMessage(Diagnostic.Kind.NOTE, "type..." + type);

        }
        //通过遍历mProxyMap，创建java文件
        for (String key : mProxyMap.keySet()) {
            ClassCreatorProxy proxyInfo = mProxyMap.get(key);
            try {
                mMessager.printMessage(Diagnostic.Kind.NOTE, " --> create " + proxyInfo.getProxyClassFullName());

                mMessager.printMessage(Diagnostic.Kind.NOTE, "getProxyClassFullName..." + proxyInfo.getProxyClassFullName());
                mMessager.printMessage(Diagnostic.Kind.NOTE, "getTypeElement..." + proxyInfo.getTypeElement());

                //createSourceFile(1,2)
                // 参数1:设置创建的类名:com.codelang.bindview.MainActivity_ViewBinding
                // 参数2：com.codelang.bindview.MainActivity
                JavaFileObject jfo = processingEnv.getFiler().createSourceFile(proxyInfo.getProxyClassFullName(), proxyInfo.getTypeElement());
                Writer writer = jfo.openWriter();
                writer.write(proxyInfo.generateJavaCode());
                writer.flush();
                writer.close();
            } catch (IOException e) {
                mMessager.printMessage(Diagnostic.Kind.NOTE, " --> create " + proxyInfo.getProxyClassFullName() + "error");
            }
        }
        mMessager.printMessage(Diagnostic.Kind.NOTE, "process finish ...");
        return true;
    }


}
