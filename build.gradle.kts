import korlibs.korge.gradle.korge

plugins {
    alias(libs.plugins.korge)
}

korge {
    id = "at.gnu.gridz"
    name = "gridZ"

    targetJvm()
    targetJs()
    targetAndroid()
//    targetDesktop()
//    targetIos()

    serializationJson()
}


dependencies {
    add("commonMainApi", project(":deps"))
//    add("commonMainApi", project(":korge-dragonbones"))
}
