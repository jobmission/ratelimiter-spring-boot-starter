
## SpringBoot 2.1.x, api request rate limiter based on redis

# 1. 添加依赖
````
        <dependency>
            <groupId>com.revengemission.commons</groupId>
            <artifactId>ratelimiter-spring-boot-starter</artifactId>
            <version>0.3</version>
        </dependency>
        
        <dependency>
             <groupId>org.springframework.data</groupId>
             <artifactId>spring-data-redis</artifactId>
         </dependency>
 
         <dependency>
             <groupId>io.lettuce</groupId>
             <artifactId>lettuce-core</artifactId>
         </dependency>       
````
# 2. application.properties 中添加配置
````
spring.redis.host=
spring.redis.port=
spring.redis.password=
````
# 3. Controller的方法上添加注解 @RateLimiter
````
    @RateLimiter
    @GetMapping("/")
    public String index(Model model) {
        return "index";
    }
````