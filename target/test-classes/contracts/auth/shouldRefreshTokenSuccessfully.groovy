package contracts.auth

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "should refresh token successfully"
    
    request {
        method POST()
        url "/api/v1/auth/refresh"
        headers {
            contentType applicationJson()
        }
        body([
            refreshToken: anyNonEmptyString()
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