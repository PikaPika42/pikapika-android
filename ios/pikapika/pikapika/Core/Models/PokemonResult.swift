//
//  PokemonResult.swift
//  pikapika
//
//  Created by Flavio on 8/18/16.
//  Copyright © 2016 Flavio. All rights reserved.
//

import Foundation
import ObjectMapper

class PokemonResult : Mappable {
    
    var id = ""
    var number = ""
    var name = ""
    //var position
    var timeleft = 0
    
    var latitude = 0.0
    var longitude = 0.0
    
    var createdAt = ""
    var expireAt = ""
    
    required init?(_ map: Map){
        
    }
    
    func mapping(map: Map){
        id          <- map["id"]
        number      <- map["number"]
        name        <- map["name"]
        timeleft    <- map["timeleft"]
        createdAt    <- map["createdAt"]
        expireAt    <- map["expireAt"]
        
        latitude    <- map["position.lat"]
        longitude    <- map["position.lng"]
    }
}