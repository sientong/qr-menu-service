package contracts.auth

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "should request password reset"
    
    request {
        method POST()
        url "/api/v1/auth/password-reset/request"
        headers {
            contentType applicationJson()
        }
        body([
            email: $(consumer(regex(email())), producer("test@example.com"))
        ])
    }
    
    response {
        status OK()
    }
} 