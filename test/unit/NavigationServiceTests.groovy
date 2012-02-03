import org.codehaus.groovy.grails.commons.*
import groovy.mock.interceptor.StubFor

class NavigationServiceTests extends GroovyTestCase {

    void testDoRegisterItemsWithImplicitSort() {
        def svc = new NavigationService()
        svc.doRegisterItem('x', [
            [controller:'rock', action:'socks', title:'off'],
            [controller:'get', action:'ur', title:'freakon']
        ])
        
        assertEquals 2, svc.byGroup['x'].size()
        assertEquals "rock", svc.byGroup['x'][0].controller
        assertEquals 0, svc.byGroup['x'][0].order
        assertEquals "get", svc.byGroup['x'][1].controller
        assertEquals 1, svc.byGroup['x'][1].order
    }
    
    void testManualRegistration() {
        def svc = new NavigationService()
        svc.registerItem('x', [controller:'rock', action:'socks', title:'off'])
        
        assertEquals 1, svc.byGroup['*'].size()
        assertEquals 1, svc.byGroup['x'].size()

        def navInfo = svc.byGroup['x'][0]
        assertEquals "rock", navInfo.controller
        assertEquals "socks", navInfo.action
        assertEquals "off", navInfo.title

        def manualInfo = svc.manuallyRegistered[0]
        assertEquals "x", manualInfo.group
        navInfo = svc.manuallyRegistered[0].info
        assertEquals "rock", navInfo.controller
        assertEquals "socks", navInfo.action
        assertEquals "off", navInfo.title
        assertEquals(['rock', "socks"], navInfo.path)
        assertEquals(['rock', "socks"], svc.reverseMapActivePathFor('rock', 'socks', null))
    }

    void testControllerRegistrationJustTrue() {
        def svc = new NavigationService()
        svc.registerItem(new DefaultGrailsControllerClass(DummyController))
        
        assertEquals 1, svc.byGroup['*'].size()
        def navInfo = svc.byGroup['*'][0]
        assertEquals "dummy", navInfo.controller
        assertEquals "Dummy", navInfo.title
        assertEquals "index", navInfo.action
        assertEquals( ['dummy', "index"], navInfo.path)
        assertEquals(['dummy', "index"], svc.reverseMapActivePathFor('dummy', 'index', null))
        assertEquals( ['dummy', "index"], svc.reverseMapActivePathFor('dummy', 'index', [:]))
    }

    void testControllerRegistrationMinimalMap() {
        def svc = new NavigationService()
        svc.registerItem(new DefaultGrailsControllerClass(DummyMapController))
        
        assertEquals 1, svc.byGroup['*'].size()
        assertEquals 1, svc.byGroup['bands'].size()
        def navInfo = svc.byGroup['bands'][0]
        assertEquals "dummyMap", navInfo.controller
        assertEquals "Mastodon", navInfo.title
        assertEquals "mastodon", navInfo.action        
        assertEquals( ['dummyMap', "mastodon"], svc.reverseMapActivePathFor('dummyMap', 'mastodon', null))
    }
    
    void testControllerRegistrationWithPath() {
        def svc = new NavigationService()
        svc.registerItem(new DefaultGrailsControllerClass(DummyMapWithPathController))

        assertEquals 3, svc.byGroup['*'].size()
        assertEquals 3, svc.byGroup['main'].size()

        def navInfo = svc.byGroup['main'][0]
        assertEquals "Mastodon", navInfo.title
        assertEquals "dummyMapWithPath", navInfo.controller
        assertEquals "mastodon", navInfo.action        
        assertEquals(["bands","mastodon"], navInfo.path)
        assertEquals( ['bands', "mastodon"], svc.reverseMapActivePathFor('dummyMapWithPath', 'mastodon', null))

        navInfo = svc.byGroup['main'][1]
        assertEquals "Revocation", navInfo.title
        assertEquals "dummyMapWithPath", navInfo.controller
        assertEquals "revocation", navInfo.action        
        assertEquals(["dummyMapWithPath","revocation"], navInfo.path)
        assertEquals( ['dummyMapWithPath', "revocation"], svc.reverseMapActivePathFor('dummyMapWithPath', 'revocation', null))

        navInfo = svc.byGroup['main'][2]
        assertEquals "Genre List", navInfo.title
        assertEquals "dummyMapWithPath", navInfo.controller
        assertEquals "genreList", navInfo.action        
        assertEquals(["genres"], navInfo.path)
        assertEquals(['genres'], svc.reverseMapActivePathFor('dummyMapWithPath', 'genreList', null))

        assertEquals(["genres","alpha"], navInfo.subItems[0].path)
        assertEquals(['genres','alpha'], svc.reverseMapActivePathFor('dummyMapWithPath', 'byAlpha', null))
        assertEquals "byAlpha", navInfo.subItems[0].action

        assertEquals(["dummyMapWithPath", "byPopularity"], navInfo.subItems[1].path)
        assertEquals(['dummyMapWithPath', 'byPopularity'], svc.reverseMapActivePathFor('dummyMapWithPath', 'byPopularity', null))
        assertEquals "byPopularity", navInfo.subItems[1].action
    }

    void testControllerRegistrationAllFeatures() {
        def svc = new NavigationService()
        svc.registerItem(new DefaultGrailsControllerClass(DummyMapFullController))
        
        assertEquals 1, svc.byGroup['*'].size()
        assertEquals 1, svc.byGroup['bands'].size()
        def navInfo = svc.byGroup['bands'][0]
        assertEquals "dummyMapFull", navInfo.controller
        assertEquals "Dillinger Escape Plan", navInfo.title
        assertEquals 5, navInfo.order
        assertEquals 'x', navInfo.params.x
        assertEquals 'y', navInfo.params.y
        
        assertEquals "dillinger", navInfo.action        
        assertEquals(['dummyMapFull', 'dillinger', '[x:x, y:y]'], navInfo.path)

        assertNotNull navInfo.subItems
        assertEquals 3, navInfo.subItems.size()     
        
        println "SubItems: ${navInfo.subItems}"   
        
        assertNotNull navInfo.subItems.find { 
            it.action == "ireWorks" && 
            it.controller == "dummyMapFull" && 
            it.title == "Ire Works" &&
            it.path == ['dummyMapFull', 'ireWorks']
        }
        assertNotNull navInfo.subItems.find { 
            it.action == "missMachine" && 
            it.controller == "dummyMapFull" && 
            it.title == "MISS MACHINE" &&
            it.path == ["dummyMapFull","missMachine"]
        }
        assertNotNull navInfo.subItems.find { 
            it.action == "calculatingInfinity" && 
            it.controller == "dummyMapFull" && 
            it.title == "Calculating Infinity"
            it.path == ["dummyMapFull","calculatingInfinity"]
        }
    }
}

class DummyController {
    static navigation = true
}

class DummyMapController {
    static navigation = [group:'bands', action:'mastodon']
}

class DummyMapWithPathController {
    static navigation = [
        [group:'main', action:'mastodon', path:'bands/mastodon'],
        [group:'main', action:'revocation'],
        [group:'main', action:'genreList', path:'genres', subItems: [
            [action:'byAlpha', path:'genres/alpha'], 
            [action:'byPopularity']
        ]]
    ]
}

class DummyMapFullController {
    static navigation = [group:'bands', title:'Dillinger Escape Plan', action:'dillinger',
        params:[x:'x', y:'y'],
        order:5, subItems:['ireWorks', [action:'missMachine', title:'MISS MACHINE'], 'calculatingInfinity']]
}
