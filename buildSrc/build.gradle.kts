/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

plugins {
    `kotlin-dsl-base`
}

repositories {
    // Mirrors the root project so that CI resolves through the local Nexus.
    val centralRepo = providers.gradleProperty("centralRepo")
    if (centralRepo.isPresent) {
        maven {
            name = "MavenCentral"
            url = uri(centralRepo.get())
            isAllowInsecureProtocol = true // Local Nexus in CI uses HTTP
        }
    } else {
        mavenCentral()
    }
}
