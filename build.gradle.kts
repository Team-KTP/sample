plugins {
    java
    id("org.springframework.boot") version "4.1.3"
    id("io.spring.dependency-management") version "1.1.7"
    id("com.diffplug.spotless") version "8.12.0"
}

group = "com.ttadak"
version = "0.0.1-SNAPSHOT"
description = "ttadak-sample"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

repositories {
    mavenCentral()
}

val archunitVersion = "1.5.1"
val lombok = "org.projectlombok:lombok"

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.2.1")

    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    runtimeOnly("com.h2database:h2")

    compileOnly(lombok)
    annotationProcessor(lombok)
    testCompileOnly(lombok)
    testAnnotationProcessor(lombok)

    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("com.tngtech.archunit:archunit-junit5:$archunitVersion")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

spotless {
    java {
        target("src/**/*.java")
        eclipse().configFile("config/spotless/eclipse-formatter.xml")
        // 아래 커스텀 단계의 코드를 바꾸면 숫자를 올린다. 그래야 Spotless가 모든 파일을 다시 검사한다.
        bumpThisNumberIfACustomStepChanges(3)
        custom("recordAnnotationNewLine", RecordAnnotationNewLine())
        removeUnusedImports()
        importOrder("\\#", "")
        trimTrailingWhitespace()
        endWithNewline()
    }
}

// record 컴포넌트의 어노테이션은 한 줄에 하나씩 두고, 필드는 그 아랫줄에 둔다. 어노테이션이 있는 record는 컴포넌트 사이를
// 한 줄 띄운다. Eclipse의 통합 옵션(insert_new_line_after_annotation)은 메서드 매개변수까지 나누고, record 전용 옵션은
// 효과가 없어서 Eclipse 다음 단계에서 record 헤더만 고친다.
class RecordAnnotationNewLine : com.diffplug.spotless.FormatterFunc, java.io.Serializable {

    override fun apply(source: String): String {
        val recordHeader =
            Regex("""^( *)(?:(?:public|protected|private|static) )*record \w+(?:<.*>)?\($""")
        val result = mutableListOf<String>()
        val components = mutableListOf<String>()
        var headerEnd: String? = null

        for (line in source.split("\n")) {
            val end = headerEnd
            if (end == null) {
                recordHeader.find(line)?.let { headerEnd = it.groupValues[1] + ")" }
                result.add(line)
            } else if (line.startsWith(end)) {
                result.addAll(format(components))
                result.add(line)
                components.clear()
                headerEnd = null
            } else {
                components.add(line)
            }
        }

        // 닫는 괄호를 찾지 못하면 그대로 둔다.
        return (result + components).joinToString("\n")
    }

    private fun format(lines: List<String>): List<String> {
        val text = """"(?:\\.|[^"\\])*""""
        val annotation = """@[\w.]+(?:\((?:[^()"]|$text|\((?:[^()"]|$text)*\))*\))?"""
        // 들여쓰기, 어노테이션들, 필드. Eclipse가 필드를 이미 다음 줄로 넘겼으면 필드는 없다.
        val annotatedLine = Regex("""^( +)($annotation(?: $annotation)*)(?: ([^@ ].*))?$""")

        val split = lines.flatMap { line ->
            val match = annotatedLine.find(line) ?: return@flatMap listOf(line)
            val (indent, annotations, component) = match.destructured
            val parts = Regex(annotation).findAll(annotations).map { it.value }.toList()

            (parts + listOfNotNull(component.ifEmpty { null })).map { indent + it }
        }

        if (split.none { it.trimStart().startsWith("@") }) {
            return split
        }

        var depth = 0
        return split.flatMapIndexed { index, line ->
            // 문자열 안의 괄호와 쉼표는 세지 않는다. 괄호나 꺾쇠가 모두 닫힌 곳의 쉼표가 컴포넌트의 끝이다.
            val code = line.replace(Regex(text), "")
            depth += code.count { it in "({<" } - code.count { it in ")}>" }
            val endsComponent = depth == 0 && code.trimEnd().endsWith(",")
            val needsBlankLine = endsComponent && split.getOrNull(index + 1)?.isNotBlank() == true

            if (needsBlankLine) listOf(line, "") else listOf(line)
        }
    }
}
