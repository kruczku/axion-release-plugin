package pl.allegro.tech.build.axion.release.domain

import com.github.zafarkhaja.semver.Version
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import pl.allegro.tech.build.axion.release.domain.scm.ScmPosition
import spock.lang.Shared
import spock.lang.Specification

class VersionFactoryTest extends Specification {

    static Project project = ProjectBuilder.builder().build()
    
    VersionConfig versionConfig = new VersionConfig(project)

    VersionFactory factory = new VersionFactory()

    def "should return current version read from position"() {
        given:
        ScmPositionContext context = new ScmPositionContext(
                new ScmPosition('master', 'release-1.0.0', true), 
                versionConfig.nextVersion
        )
        
        when:
        Version version = factory.create(context, versionConfig, VersionReadOptionsFactory.empty())

        then:
        version.toString() == '1.0.0'
    }

    def "should return current version with patch version increased when not on tag"() {
        given:
        ScmPositionContext context = new ScmPositionContext(
                new ScmPosition('master', 'release-1.0.0', false),
                versionConfig.nextVersion
        )

        when:
        Version version = factory.create(context, versionConfig, VersionReadOptionsFactory.empty())

        then:
        version.toString() == '1.0.1'
    }

    def "should return forced version when forcing is on"() {
        given:
        ScmPositionContext context = new ScmPositionContext(
                new ScmPosition('master', 'release-1.0.0', true),
                versionConfig.nextVersion
        )

        when:
        Version version = factory.create(context, versionConfig, VersionReadOptionsFactory.withForcedVersion('2.0.0'))

        then:
        version.toString() == '2.0.0'
    }

    def "should return custom initial version when no tag exists"() {
        given:
        ScmPositionContext context = new ScmPositionContext(
                new ScmPosition('master', null, false),
                versionConfig.nextVersion
        )
        versionConfig.tag.initialVersion = { r, p -> '0.0.1' }

        when:
        Version version = factory.create(context, versionConfig, VersionReadOptionsFactory.empty())

        then:
        version.toString() == '0.0.1'
    }

    def "should return default initial version when no tag exists and initial version not explicitly defined"() {
        given:
        ScmPositionContext context = new ScmPositionContext(
                new ScmPosition('master', null, false),
                versionConfig.nextVersion
        )

        when:
        Version version = factory.create(context, versionConfig, VersionReadOptionsFactory.empty())

        then:
        version.toString() == '0.1.0'
    }

    def "should deserialize nextVersion before deserializing version when on nextVersion tag"() {
        given:
        ScmPositionContext context = new ScmPositionContext(
                new ScmPosition('master', 'release-2.0.0-alpha', false),
                versionConfig.nextVersion
        )

        when:
        Version version = factory.create(context, versionConfig, VersionReadOptionsFactory.empty())

        then:
        version.toString() == '2.0.0'
    }
    
    def "should not increment patch version when being on position after next version tag"() {
        given:
        ScmPositionContext context = new ScmPositionContext(
                new ScmPosition('master', 'release-2.0.0-alpha', false),
                versionConfig.nextVersion
        )

        when:
        Version version = factory.create(context, versionConfig, VersionReadOptionsFactory.empty())

        then:
        version.toString() == '2.0.0'
    }
}
