import XCTest
@testable import PorchFest

final class AppEnvironmentTests: XCTestCase {
    func testReadsBaseURLAndNameFromInfoDictionary() {
        let environment = AppEnvironment(infoDictionary: [
            "PFBaseURL": "http://localhost:8000",
            "PFEnvironment": "local",
        ])

        XCTAssertEqual(environment.baseURL, "http://localhost:8000")
        XCTAssertEqual(environment.name, "local")
        XCTAssertEqual(environment.sharedConfig.baseUrl, "http://localhost:8000")
    }

    func testFallsBackToProductionWhenConfigIsMissing() {
        let environment = AppEnvironment(infoDictionary: ["PFBaseURL": "  "])

        XCTAssertEqual(environment.baseURL, "https://towerporchfest.org")
        XCTAssertEqual(environment.name, "prod")
    }

    func testLocalSchemeIsWiredToTheLocalBackend() {
        // The test bundle is hosted by the app built with the Debug-Local configuration.
        XCTAssertEqual(AppEnvironment.current.name, "local")
        XCTAssertTrue(AppEnvironment.current.baseURL.hasPrefix("http://"))
    }
}
