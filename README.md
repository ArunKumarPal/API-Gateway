## API-Gateway

this is a sample apigateway for API. 

# 
i) It have token validation we can use multiple token validator based on header key 'X-AUTH-VALIDATOR' without header it verify token with default validator 

ii) Using this we can create multiple routes yml file (this is useful if we have lots of API and we need to divide the routes in multiple files)

iii) Using this we can rate limit of API both token user base ya Ip base by default its token user for ip based we need to add header (IP_RATE_LIMIT) in api 
