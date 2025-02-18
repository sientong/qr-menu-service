package contracts.auth

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "should logout successfully"
    
    request {
        method POST()
        url "/api/v1/auth/logout"
        headers {
            header("Authorization", "Bearer ${anyNonEmptyString()}")
        }
    }
    
    response {
        status OK()
    }
} 