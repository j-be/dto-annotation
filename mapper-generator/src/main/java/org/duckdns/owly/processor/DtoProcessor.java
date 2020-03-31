package org.duckdns.owly.processor;

import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.util.*;
import java.util.Map.Entry;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;

import org.apache.commons.lang.WordUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.duckdns.owly.annotation.Dto;

@SupportedAnnotationTypes({ "org.duckdns.owly.annotation.Dto" })
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class DtoProcessor extends AbstractProcessor {
	private VelocityEngine velocityEngine = null;

	@Override
	public synchronized void init(javax.annotation.processing.ProcessingEnvironment processingEnv) {
		super.init(processingEnv);
		this.initializeVelocity();
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		String fqTargetClassName = null;
		String targetClassName = null;
		String sourceClassName = null;
		String packageName = null;
		List<String> fields = new ArrayList<>();

		this.debug(this.getClass().getSimpleName());
		for (Element e : roundEnv.getElementsAnnotatedWith(Dto.class)) {
			if (e.getKind() == ElementKind.CLASS) {

				TypeElement classElement = (TypeElement) e;
				PackageElement packageElement = (PackageElement) classElement.getEnclosingElement();

				this.debug("annotated class: " + classElement.getQualifiedName());

				fqTargetClassName = classElement.getQualifiedName().toString();
				targetClassName = classElement.getSimpleName().toString();
				packageName = packageElement.getQualifiedName().toString();

				this.debug("get source class: " + sourceClassName);
				AnnotationMirror mirror = DtoProcessor.getAnnotationMirror(classElement, Dto.class.getName());
				this.debug("mirror: " + mirror);
				this.debug("Value: " + this.getAnnotationValueAsType(mirror, "entity"));
				sourceClassName = this.getAnnotationValueAsType(mirror, "entity").getSimpleName().toString();
				this.debug("got source class: " + sourceClassName);
				for (VariableElement field : ElementFilter.fieldsIn(e.getEnclosedElements()))
					fields.add(WordUtils.capitalize(field.getSimpleName().toString()));
				this.debug("got all information");
			}
		}

		this.debug(fields.toString());
		if (fqTargetClassName != null)
			this.generateMappers(packageName + ".mapper", sourceClassName, targetClassName, fields,
					Collections.singletonList(packageName + ".*"));

		return true;
	}

	private void initializeVelocity() {
		Properties props = new Properties();
		URL url = this.getClass().getClassLoader().getResource("velocity.properties");

		this.debug("get properties");

		try {
			props.load(url.openStream());
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		this.velocityEngine = new VelocityEngine(props);
		this.velocityEngine.init();
		this.debug("velocity engine initialized");
	}

	private void generateMappers(String packageName, String sourceClassName, String targetClassName,
			List<String> fields, List<String> imports) {
		VelocityContext vc = new VelocityContext();

		vc.put("packageName", packageName);
		vc.put("targetClass", targetClassName);
		vc.put("sourceClass", sourceClassName);
		vc.put("fields", fields);
		vc.put("imports", imports);

		Template vt = this.velocityEngine.getTemplate("dto.vm");

		try (Writer writer = this.processingEnv.getFiler()
				.createSourceFile(String.format("%s.%sTo%sMapper", packageName, targetClassName, sourceClassName))
				.openWriter()) {
			vt.merge(vc, writer);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	private void debug(String message) {
		String fullMessage = String.format("%s: %s", this.getClass().getSimpleName(), message);
		this.processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, fullMessage);
	}

	private static AnnotationMirror getAnnotationMirror(TypeElement typeElement, String className) {
		for (AnnotationMirror m : typeElement.getAnnotationMirrors()) {
			if (m.getAnnotationType().toString().equals(className)) {
				return m;
			}
		}
		return null;
	}

	private static AnnotationValue getAnnotationValue(AnnotationMirror annotationMirror, String key) {
		for (Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : annotationMirror.getElementValues()
				.entrySet()) {
			if (entry.getKey().getSimpleName().toString().equals(key)) {
				return entry.getValue();
			}
		}
		return null;
	}

	private TypeElement getAnnotationValueAsType(AnnotationMirror annotationMirror, String key) {
		AnnotationValue annotationValue = DtoProcessor.getAnnotationValue(annotationMirror, key);
		this.debug("annotationValue: " + annotationValue);
		if (annotationValue == null) {
			return null;
		}
		TypeMirror typeMirror = (TypeMirror) annotationValue.getValue();
		this.debug("typeMirror: " + typeMirror);

		if (typeMirror == null) {
			return null;
		}
		return (TypeElement) this.processingEnv.getTypeUtils().asElement(typeMirror);
	}
}
