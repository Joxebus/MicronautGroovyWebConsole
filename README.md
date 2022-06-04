# MicronautGroovyWebConsole

This project is aimed for those people who want to have an online Groovy Console
compatible with Groovy 3.0.6, this project is builtin with Micronaut.

## Technologies

- Groovy 3.0.9
- Micronaut 3.3.0  
- Gradle 7.3.3
- Thymeleaf 2.2.1
- Bootstrap 5

## Preview

![online-groovy-console](img/console.png)

## Build project

```shell
./gradlew build
```

## Run project

```shell
java -Dmicronaut.environments=local -jar build/libs/MicronautGroovyWebConsole-1.0-all.jar
```

**Output**
```shell
 __  __ _                                  _   
|  \/  (_) ___ _ __ ___  _ __   __ _ _   _| |_ 
| |\/| | |/ __| '__/ _ \| '_ \ / _` | | | | __|
| |  | | | (__| | | (_) | | | | (_| | |_| | |_ 
|_|  |_|_|\___|_|  \___/|_| |_|\__,_|\__,_|\__|
  Micronaut (v3.3.0)

16:36:37.525 [main] INFO  i.m.context.env.DefaultEnvironment - Established active environments: [local]
16:36:44.328 [main] INFO  io.micronaut.runtime.Micronaut - Startup completed in 7306ms. Server Running: http://localhost:8089
```

There are 3 available profiles `local`, `dropbox` and `heroku`

- `local`: Use a local storage to save files at configured at `application.yml`
- `dropbox`: Use the dropbox configuration to save files also you may need to provide `DROPBOX_TOKEN` 
setting up directly at `application-dropbox.yml` or by env variables.

## Code Samples

You can see some [code samples](https://gist.github.com/Joxebus "Joxebus's Gists") at my gist.

## License

This project is under [Apache 2.0 License](LICENSE "See license here")