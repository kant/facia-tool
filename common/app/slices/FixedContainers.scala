package slices

import model.Content

object TagContainers {
  import ContainerDefinition.{ofSlices => slices}

  val allTagPage = slices(
    QuarterQuarterQuarterQuarter,
    TlTlTl,
    TlTlTl,
    TlTlTl,
    TlTlTl,
    TlTlTl,
    TlTlTl,
    TlTlMpu
  )

  val tagPage = slices(
    HalfQQ,
    QuarterQuarterQuarterQuarter,
    TlTlTl,
    TlTlMpu
  )

  val contributorTagPage =  slices(
    HalfQl4Ql4,
    TlTlTl,
    TlTlTl,
    TlTlMpu
  )

  val keywordPage = slices(
    TTT,
    TlTlTl,
    TlTlTl,
    TlTlMpu
  )
}

object FixedContainers {
  import ContainerDefinition.{ofSlices => slices}

  //TODO: Temporary vals for content until we refactor
  val fixedSmallSlowI = slices(Full)
  val fixedSmallSlowII = slices(HalfHalf)
  val fixedSmallSlowIV = slices(QuarterQuarterQuarterQuarter)
  val fixedSmallSlowVI = slices(TTTL4)
  val fixedMediumSlowVI = slices(ThreeQuarterQuarter, QuarterQuarterQuarterQuarter)
  val fixedMediumSlowVII = slices(HalfQQ, QuarterQuarterQuarterQuarter)
  val fixedMediumSlowXIIMpu = slices(TTT, TlTlMpu)
  val fixedMediumFastXI = slices(HalfQQ, Ql2Ql2Ql2Ql2)
  val fixedMediumFastXII = slices(QuarterQuarterQuarterQuarter, Ql2Ql2Ql2Ql2)

  val all: Map[String, ContainerDefinition] = Map(
    ("fixed/small/slow-I", slices(Full)),
    ("fixed/small/slow-II", slices(HalfHalf)),
    ("fixed/small/slow-III", slices(HalfQQ)),
    ("fixed/small/slow-IV", fixedSmallSlowIV),
    ("fixed/small/slow-V-half", slices(Hl4Half)),
    ("fixed/small/slow-V-third", slices(QuarterQuarterHl3)),
    ("fixed/small/slow-V-mpu", slices(TTlMpu)),
    ("fixed/small/slow-VI", fixedSmallSlowVI),
    ("fixed/small/fast-VIII", slices(QuarterQuarterQlQl)),
    ("fixed/small/fast-X", slices(QuarterQlQlQl)),
    ("fixed/medium/slow-VI", fixedMediumSlowVI),
    ("fixed/medium/slow-VII", fixedMediumSlowVII),
    ("fixed/medium/slow-XII-mpu", fixedMediumSlowXIIMpu),
    ("fixed/medium/fast-XI", fixedMediumFastXI),
    ("fixed/medium/fast-XII", fixedMediumFastXII),
    ("fixed/large/slow-XIV", slices(ThreeQuarterQuarter, QuarterQuarterQuarterQuarter, Ql2Ql2Ql2Ql2)),
    ("fixed/large/fast-XV", slices(HalfQQ, Ql3Ql3Ql3Ql3))
  )

  def unapply(collectionType: Option[String]): Option[ContainerDefinition] =
    collectionType.flatMap(all.lift)
}

object DynamicContainers {
  val all: Map[String, DynamicContainer] = Map(
    ("dynamic/fast", DynamicFast),
    ("dynamic/slow", DynamicSlow)
  )

  def apply(collectionType: Option[String], items: Seq[Content]): Option[ContainerDefinition] = {
    for {
      typ <- collectionType
      dynamicContainer <- all.get(typ)
      definition <- dynamicContainer.containerDefinitionFor(items.map(Story.fromContent))
    } yield definition
  }
}