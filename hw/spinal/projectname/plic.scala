package projectname

import spinal.core._
import spinal.lib._
import spinal.lib.misc._
import spinal.lib.misc.plic._
import spinal.lib.bus.amba3.apb._

class Apb3Plic(sourceCount : Int, targetCount : Int) extends Component{
  val priorityWidth = 2
  val plicMapping = PlicMapping.sifive
  import plicMapping._

  val io = new Bundle {
    val bus = slave(Apb3(Apb3Config(22,32)))
    val sources = in Bits (sourceCount bits)
    val targets = out Bits (targetCount bits)
  }

  val gateways = (for ((source, id) <- (io.sources.asBools, 1 to sourceCount).zipped) yield PlicGatewayActiveHigh(
    source = source,
    id = id,
    priorityWidth = priorityWidth
  )).toSeq

  val targets = for (i <- 0 until targetCount) yield PlicTarget(
    id = i,
    gateways = gateways,
    priorityWidth = priorityWidth
  )

  io.targets := targets.map(_.iep).asBits

  val bus = Apb3SlaveFactory(io.bus)
  val mapping = PlicMapper(bus, plicMapping)(
    gateways = gateways,
    targets = targets
  )
}
