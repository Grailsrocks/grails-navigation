import grails.test.TagLibUnitTestCase

class NavigationTagLibTests extends TagLibUnitTestCase {
    protected void setUp() {
        super.setUp()
        loadCodec(org.codehaus.groovy.grails.plugins.codecs.HTMLCodec)
    }
    
    /*
    
    These are commented out because I can't work out why Grails will not use our mocked service
    ...can't unit test really because we need the createLink tag to work in order to test this stuff
*/    
    void testEachItemByController() {
        tagLib.navigationService = [
            byGroup: ['tabs': [ 
                    [controller:'dummy', action:'index', path:['dummy', 'index']],
                    [controller:'dummy', action:'get', path:['dummy', 'get']]
                ]
            ],
            reverseMapActivePathFor: { con, act, params -> [con, act]}
        ]

        tagLib.metaClass.controllerName = 'something'
        tagLib.metaClass.actionName = 'something'
        tagLib.metaClass.createLink = { args -> "link" }

        tagLib.eachItem([controller:'dummy', group:'tabs'], {
            "Action:${it.action}|Active:${it.active}&"
        })
        def outcome = tagLib.out.toString().split('&')
        
        assertEquals 'Action:index|Active:false', outcome[0]
        assertEquals 'Action:get|Active:false', outcome[1]
    }

    void testEachItemActiveByPathDeepHit() {
        tagLib.navigationService = [
            byGroup: ['tabs': [ 
                    [controller:'dummy', action:'index', title:'Dummy', path:['dummy', 'index']],
                    [controller:'dummy', action:'get', title:'Get', path:['something', 'else', 'here'],
                        subItems:[ [action:'search', path:['something', 'else', 'here', 'searching']] ]
                    ]
                ]
            ],
            reverseMapActivePathFor: { con, act, params -> [con, act]}
        ]

        tagLib.metaClass.controllerName = 'something'
        tagLib.metaClass.actionName = 'something'
        tagLib.metaClass.createLink = { args -> "link" }

        def first = true
        tagLib.eachItem([activePath:'something/else/here', group:'tabs'], {
            "Action:${it.action}|Active:${it.active}|Title:${it.title}&"
        })
        def outcome = tagLib.out.toString().split('&')
        
        assertEquals 'Action:index|Active:false|Title:Dummy', outcome[0]
        assertEquals 'Action:get|Active:true|Title:Get', outcome[1]
    }
    

    void testEachItemActiveByPathSubItemHit() {
        tagLib.navigationService = [
            byGroup: ['tabs': [ 
                    [controller:'dummy', action:'index', title:'Dummy', path:['dummy', 'index']],
                    [controller:'dummy', action:'get', title:'Get', path:['something', 'else', 'here'],
                        subItems:[ [action:'search', path:['something', 'else', 'here', 'searching']] ]
                    ]
                ]
            ],
            reverseMapActivePathFor: { con, act, params -> [con, act]}
        ]

        tagLib.metaClass.controllerName = 'something'
        tagLib.metaClass.actionName = 'something'
        tagLib.metaClass.createLink = { args -> "link" }

        def first = true
        tagLib.eachItem([activePath:'something/else/here', group:'tabs'], {
            "Action:${it.action}|Active:${it.active}|Title:${it.title}&"
        })
        def outcome = tagLib.out.toString().split('&')
        
        assertEquals 'Action:index|Active:false|Title:Dummy', outcome[0]
        assertEquals 'Action:get|Active:true|Title:Get', outcome[1]
    }
    
    void testEachSubItemActiveByPathDeepHit() {
        tagLib.navigationService = [
            byGroup: ['tabs': [ 
                    [controller:'dummy', action:'index', title:'Dummy', path:['dummy', 'index']],
                    [controller:'dummy', action:'get', title:'Get', path:['something', 'else', 'here'],
                        subItems:[ [action:'search', path:['something', 'else', 'here', 'searching']]]
                    ]
                ]
            ],
            reverseMapActivePathFor: { con, act, params -> [con, act]}
        ]

        tagLib.metaClass.controllerName = 'something'
        tagLib.metaClass.actionName = 'something'
        tagLib.metaClass.createLink = { args -> "link" }

        def first = true
        tagLib.eachSubItem([activePath:'something/else/here/searching', group:'tabs'], {
            "Action:${it.action}|Active:${it.active}|Title:${it.title}&"
        })
        def outcome = tagLib.out.toString().split('&')
        
        assertEquals 'Action:search|Active:true|Title:null', outcome[0]
    }

    void testEachSubItemNotActiveByPathDeepHit() {
        tagLib.navigationService = [
            byGroup: ['tabs': [ 
                    [controller:'dummy', action:'index', title:'Dummy', path:['dummy', 'index']],
                    [controller:'dummy', action:'get', title:'Get', path:['something', 'else', 'here'],
                        subItems:[ [action:'search', path:['something', 'else', 'here', 'searching']]]
                    ]
                ]
            ],
            reverseMapActivePathFor: { con, act, params -> [con, act]}
        ]

        tagLib.metaClass.controllerName = 'something'
        tagLib.metaClass.actionName = 'something'
        tagLib.metaClass.createLink = { args -> "link" }

        def first = true
        tagLib.eachSubItem([activePath:'something/else/here', group:'tabs'], {
            "Action:${it.action}|Active:${it.active}|Title:${it.title}&"
        })
        def outcome = tagLib.out.toString().split('&')
        
        assertEquals 'Action:search|Active:false|Title:null', outcome[0]
    }

    void testRenderSubItems() {
        tagLib.navigationService = [
            byGroup: ['tabs':
                [ 
                    [controller:'dummy', action:'index', title:'Dummy', path:['dummy', 'index']],
                    [controller:'dummy', action:'get', title:'Get', path:['dummy', 'get'],
                        subItems:[ 
                            [action:'search', path:['dummy', 'search']],
                            [action:'test', path:['dummy', 'test']]
                        ]
                    ]
                ]
            ],
            reverseMapActivePathFor: { con, act, params -> [con, act]}
        ]

        tagLib.metaClass.controllerName = 'dummy'
        tagLib.metaClass.actionName = 'get'
        tagLib.metaClass.message = { args -> args.code }
        tagLib.metaClass.createLink = { args -> "link" }

        def first = true
        tagLib.renderSubItems([group:'tabs'])
        def outcome = tagLib.out.toString()

        println outcome
        assertTrue outcome.contains('search')
        assertTrue outcome.contains('test')
    }

    void testRenderSubItemsPathInSubItem() {
        tagLib.navigationService = [
            byGroup: ['tabs':
                [ 
                    [controller:'dummy', action:'index', title:'Dummy', path:['dummy', 'index']],
                    [controller:'dummy', action:'get', title:'Get', path:['dummy', 'get'],
                        subItems:[ 
                                [action:'search', path:['dummy', 'search']],
                                [action:'test', path:['dummy', 'test']]
                        ]
                    ]
                ]
            ],
            reverseMapActivePathFor: { con, act, params -> [con, act]}
        ]

        tagLib.metaClass.controllerName = 'dummy'
        tagLib.metaClass.actionName = 'test'
        tagLib.metaClass.message = { args -> args.code }
        tagLib.metaClass.createLink = { args -> "link" }

        def first = true
        tagLib.renderSubItems([group:'tabs'])
        def outcome = tagLib.out.toString()

        println outcome
        assertTrue outcome.contains('search')
        assertTrue outcome.contains('test')
    }
    
    void testDoPathsIntersect() {
        def cases = [
            [a:['dummy', 'test'], b:['dummy'], result: true],
            [a:['dummy', 'test'], b:['dummy', 'search'], result: true],
            [a:['dummy'],         b:['dummy', 'search'], result: true],
            [a:['something'],     b:['dummy', 'search'], result: false]
        ]
        cases.each { e ->
            assertEquals "Case ${e} failed", e.result, tagLib.doPathsIntersect(e.a, e.b) 
        }
    }

    void testPathShouldBeActive() {
        def cases = [
            [itemPath:['dummy'],            currentPath:['dummy', 'x'],      result: true],
            [itemPath:['dummy', 'x'],       currentPath:['dummy', 'x'],      result: true],
            [itemPath:['dummy', 'x', 'y'],  currentPath:['dummy', 'x'],      result: false],
            [itemPath:['dummy', 'test'],    currentPath:['dummy'],           result: false],
            [itemPath:['dummy', 'test'],    currentPath:['dummy', 'x'],      result: false],
            [itemPath:['dummy'],            currentPath:['dummy'],           result: true],
            [itemPath:[],                   currentPath:['dummy'],           result: false],
            [itemPath:['something'],        currentPath:['dummy'],           result: false]
        ]
        cases.each { e ->
            assertEquals "Case ${e} failed", e.result, tagLib.pathIsActive(e.itemPath, e.currentPath) 
        }
    }

    void testIsPathFullyEncapsulatedBy() {
        def cases = [
            [a:['dummy', 'test'], b:['dummy'], result: false],
            [a:['dummy', 'test'], b:['dummy', 'search'], result: false],
            [a:['dummy'],         b:['dummy', 'search'], result: true],
            [a:['something'],     b:['dummy', 'search'], result: false]
        ]
        cases.each { e ->
            assertEquals "Case ${e} failed", e.result, tagLib.isPathFullyEncapsulatedBy(e.a, e.b) 
        }
    }
}
