import command.HostCommand
import external.yargs.yargs
import kotlinx.serialization.ExperimentalSerializationApi

fun greeting(name: String) =
    "Hello, $name"

fun main() {
    println(greeting("ology-kt"))
    yargs.usage("Usage: $0 <command> [options]")
        .command(HostCommand())
        .recommendCommands()
        .demandCommand(1)
        .strict()
        .help("h")
        .alias("h", "help")
        .argv
}
