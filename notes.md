Automerge Kotlin has to be initialized before being used.
Example:

````kotlin
// Android
fun main() {
    AutomergeKotlin.init(context)
    
    DefaultDocumentRepository.init()
}
````

````kotlin
// Desktop
fun main() {
    AutomergeKotlin.init()
}
````

### Dependencies

Each target platform has to provide the following dependencies:
- RSocketManager instance - This creates and provides RSocket connections. Depends on Ktor client/server implementation.
