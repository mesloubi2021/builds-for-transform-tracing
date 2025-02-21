plugins {
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.apache.commons:commons-text:1.10.0")
    implementation(project(":lib1"))
}

application {
    mainClass.set("org.example.app.App")
}

val artifactType = Attribute.of("artifactType", String::class.java)
val unrelated = Attribute.of("unrelated", Boolean::class.javaObjectType)
val renamed = Attribute.of("renamed", Boolean::class.javaObjectType)
dependencies {
    attributesSchema {
        attribute(renamed)
        attribute(unrelated)
    }
    artifactTypes.getByName("jar") {
        attributes.attribute(renamed, false)
        attributes.attribute(unrelated, false)
    }
}

configurations.all {
    afterEvaluate {
        if (isCanBeResolved) {
            attributes.attribute(renamed, true)
            attributes.attribute(unrelated, false)
        }
    }
}

dependencies {
    registerTransform(Renamer::class) {
        from.attribute(renamed, false).attribute(artifactType, "jar")
        to.attribute(renamed, true).attribute(artifactType, "jar")

        parameters {
            postfix = "-renamed"
        }
    }
}

abstract class Renamer : TransformAction<Renamer.Parameters> {
    interface Parameters : TransformParameters {
        @get:Input
        var postfix: String
    }

    @get:PathSensitive(PathSensitivity.NAME_ONLY)
    @get:InputArtifact
    abstract val inputArtifact: Provider<FileSystemLocation>

    @get:InputArtifactDependencies
    abstract val dependencies: FileCollection

    override fun transform(outputs: TransformOutputs) {
        val postfix = parameters.postfix
        val inputFile = inputArtifact.get().asFile
        val outputFile = outputs.file(inputFile.nameWithoutExtension + postfix + ".jar")
        inputFile.copyTo(outputFile)
        println("Renamed ${inputFile.name} to ${outputFile.name}")
    }
}