Wicket Localization Checker Plugin
==================================

A maven2 plugin for checking the localization files in a wicket project.

This plugin works by scanning project base dir for .properties files and verifies if all the properties files contain the same labels.It also checks for the existence of the other locale properties
files as specified in the checkLocales tag.

The plugin currently supports following options:

- strictChecking : When set to true the build fails if there any problems with the .properties files or missing labels.
- checkLocales: The list of locales to check for.
- quiet: Runs in quiet mode, by printing less info on the console.


Usage
-----
 Add the plugin to your build plugins:

	  <plugin>
		<groupId>com.vijaykiran.wicket.utils</groupId>
		<artifactId>localization-checker-plugin</artifactId>
		<version>0.1-SNAPSHOT</version>
		<configuration>
			<strictChecking>true</strictChecking>
			<quiet>false</quiet>
			<checkLocales>nl,fr,it</checkLocales>
		</configuration>
		<executions>
                <execution>
                    <goals>
                        <goal>check</goal>
                    </goals>
                </execution>
            </executions>
	  </plugin>
