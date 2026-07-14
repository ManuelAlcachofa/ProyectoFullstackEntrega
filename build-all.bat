rem $env:DOCKER_BUILDKIT=0
rem docker rm -f $(docker ps -aq)
FOR /f %%i IN ('docker ps -aq') DO docker rm -f %%i
FOR /f %%i IN ('docker images -aq') DO docker rmi -f %%i
cd ApiGateway
call .\mvnw clean package -DskipTests

cd ../UsuarioService
call .\mvnw clean package -DskipTests

cd ../LibroService
call .\mvnw clean package -DskipTests

cd ../AutorService
call .\mvnw clean package -DskipTests

cd ../CategoriaService
call .\mvnw clean package -DskipTests

cd ../InventarioService
call .\mvnw clean package -DskipTests

cd ../PrestamoService
call .\mvnw clean package -DskipTests

cd ../ReservaService
call .\mvnw clean package -DskipTests

cd ../MultaService
call .\mvnw clean package -DskipTests

cd ../NotificacionService
call .\mvnw clean package -DskipTests

cd ../ResenaService
call .\mvnw clean package -DskipTests
