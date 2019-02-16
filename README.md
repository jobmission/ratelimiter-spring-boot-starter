
## SpringBoot 2.x, api request rate limiter based on redis

# 1. 添加依赖
````
        <dependency>
            <groupId>com.revengemission.commons</groupId>
            <artifactId>ratelimiter-spring-boot-starter</artifactId>
            <version>0.0.1-SNAPSHOT</version>
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
rate.limiter.host=
rate.limiter.port=
rate.limiter.password=
````
# 3. Controller的方法上添加注解 @RateLimiter
````
    @RateLimiter
    @RequestMapping("/")
    public String index(Model model) {
        return "index";
    }
````