A maven2 plugin for checking the localization files in a wicket project.

 How To:

 Add the plugin to your <build><plugins>


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
