//
//  ViewController.swift
//  pikapika
//
//  Created by Flavio on 8/18/16.
//  Copyright © 2016 Flavio. All rights reserved.
//

import UIKit
import MapKit
import SwiftHTTP
import ObjectMapper

class ViewController: UIViewController, CLLocationManagerDelegate, MKMapViewDelegate {
    
    @IBOutlet weak var mapView: MKMapView!
    
    var currentLocation = CLLocationCoordinate2D()
    let locationManager = CLLocationManager()
    var pokemonList = [PokemonResult]()
    var firstLoad = true
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        self.mapView.delegate = self
        
        // For use in foreground
        self.locationManager.requestWhenInUseAuthorization()
        
        if CLLocationManager.locationServicesEnabled() {
            locationManager.delegate = self
            locationManager.distanceFilter = kCLDistanceFilterNone
            locationManager.desiredAccuracy = kCLLocationAccuracyHundredMeters
            locationManager.startUpdatingLocation()
            locationManager.startMonitoringSignificantLocationChanges()
            if locationManager.location != nil {
                currentLocation = locationManager.location!.coordinate
                firstLoad = false
                requestPokemon()
            }
            
        }
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    func locationManager(manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        currentLocation = manager.location!.coordinate
        //print("locations = \(locValue.latitude) \(locValue.longitude)")
        if firstLoad{
            requestPokemon()
            firstLoad = !firstLoad
        }
    }
    
    func mapView(mapView: MKMapView, viewForAnnotation annotation: MKAnnotation) -> MKAnnotationView? {
        if annotation is MKUserLocation {
            //return nil so map view draws "blue dot" for standard user location
            return nil
        }
        
        let reuseId = "pin"
        
        var pinView = mapView.dequeueReusableAnnotationViewWithIdentifier(reuseId) as? MKPinAnnotationView
        if pinView == nil {
            pinView = MKPinAnnotationView(annotation: annotation, reuseIdentifier: reuseId)
            pinView!.canShowCallout = true
            pinView!.animatesDrop = true
            pinView!.pinColor = .Purple
        }
        else {
            pinView!.annotation = annotation
        }
        
        return pinView
    }
    
    func requestPokemon(){
        centerMapOnLocation(currentLocation)
        do {
            let opt = try HTTP.GET("https://api.pikapika.io/pokemons/\(currentLocation.latitude)/\(currentLocation.longitude)?radius=5000")
            opt.start { response in
                if let err = response.error {
                    print("error: \(err.localizedDescription)")
                    return //also notify app of failure as needed
                }
                if response.statusCode == 200 {
                    //parse json
                    let parsedJSON = self.parseJSONString(response.text!)
                    if  parsedJSON != nil  {
                        if let pokemonList = Mapper<PokemonResult>().mapArray(parsedJSON!["data"]){
                            self.pokemonList = pokemonList
                            self.drawMarkers()
                            print("result list:\(pokemonList.count)")
                        }
                    }
                }
            }
        } catch let error {
            print("got an error creating the request: \(error)")
        }
    }
    
    func drawMarkers(){
        for pokemon in pokemonList {
            let coord = CLLocationCoordinate2DMake(pokemon.latitude, pokemon.longitude)
            let dropPin = MKPointAnnotation()
            dropPin.coordinate = coord
            dropPin.title = pokemon.name
            mapView.addAnnotation(dropPin)
        }
    }
    
    func parseJSONString(JSON: String) -> AnyObject? {
        let data = JSON.dataUsingEncoding(NSUTF8StringEncoding, allowLossyConversion: true)
        if let data = data {
            let parsedJSON: AnyObject?
            do {
                parsedJSON = try NSJSONSerialization.JSONObjectWithData(data, options: NSJSONReadingOptions.AllowFragments)
            } catch let error {
                print(error)
                parsedJSON = nil
            }
            return parsedJSON
        }
        
        return nil
    }


    let regionRadius: CLLocationDistance = 1000
    func centerMapOnLocation(location: CLLocationCoordinate2D) {
        let coordinateRegion = MKCoordinateRegionMakeWithDistance(location,
                                                                  regionRadius * 2.0, regionRadius * 2.0)
        mapView.setRegion(coordinateRegion, animated: true)
    }
}

