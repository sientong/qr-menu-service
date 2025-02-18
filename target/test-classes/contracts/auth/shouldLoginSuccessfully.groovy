package contracts.auth

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "should login successfully"
    
    request {
        method POST()
        url "/api/v1/auth/login"
        headers {
            contentType applicationJson()
        }
        body([
            email: "test@example.com",
            password: "password123"
        ])
    }
    
    response {
        status OK()
        headers {
            contentType applicationJson()
        }
        body([
            accessToken: anyNonEmptyString(),
            refreshToken: anyNonEmptyString(),
            tokenType: "Bearer",
            expiresIn: anyNumber()
        ])
    }
} 