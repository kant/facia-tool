package layout

import cards._
import BrowserWidth._
import views.support.Profile

object FaciaWidths {
  private val MediaMobile = Map[CardType, BrowserWidth](
    (MediaList, 127.px),
    (Standard, 100.perc)
  )

  val ExtraPixelWidthsForMediaMobile: Seq[PixelWidth] = List(
    445.px, // largest width for mobile breakpoint
    605.px  // largest width for mobile landscape breakpoint
  )

  private val CutOutMobile = Map[CardType, BrowserWidth](
    (MediaList, 115.px),
    (Standard, 130.px)
  )

  private val MediaTablet = Map[CardType, BrowserWidth](
    (MediaList, 140.px),
    (Fluid, 140.px),
    (Standard, 160.px),
    (Third, 220.px),
    (Half, 340.px),
    (ThreeQuarters, 340.px),
    (ThreeQuartersRight, 340.px),
    (FullMedia50, 350.px),
    (FullMedia75, 520.px),
    (FullMedia100, 700.px)
  )

  private val CutOutTablet = Map[CardType, BrowserWidth](
    (MediaList, 115.px),
    (Standard, 216.px),
    (Third, 187.px),
    (Half, 331.px),
    (ThreeQuarters, 331.px),
    (ThreeQuartersRight, 331.px),
    (FullMedia50, 331.px),
    (FullMedia75, 331.px),
    (FullMedia100, 331.px)
  )

  private val MediaDesktop = Map[CardType, BrowserWidth](
    (MediaList, 140.px),
    (Fluid, 188.px),
    (Standard, 220.px),
    (Third, 300.px),
    (Half, 460.px),
    (ThreeQuarters, 460.px),
    (ThreeQuartersRight, 460.px),
    (FullMedia50, 470.px),
    (FullMedia75, 700.px),
    (FullMedia100, 940.px)
  )

  private val CutOutDesktop = Map[CardType, BrowserWidth](
    (MediaList, 115.px),
    (Standard, 216.px),
    (Third, 216.px),
    (Half, 331.px),
    (ThreeQuarters, 389.px),
    (ThreeQuartersRight, 389.px),
    (FullMedia50, 331.px),
    (FullMedia75, 331.px),
    (FullMedia100, 331.px)
  )

  def mediaFromItemClasses(itemClasses: ItemClasses) = {
    val desktopClass = itemClasses.desktop.getOrElse(itemClasses.tablet)

    WidthsByBreakpoint(
      mobile          = MediaMobile.get(itemClasses.mobile),
      tablet          = MediaTablet.get(itemClasses.tablet),
      desktop         = MediaDesktop.get(desktopClass)
    )
  }

  def cutOutFromItemClasses(itemClasses: ItemClasses) = {
    val desktopClass = itemClasses.desktop.getOrElse(itemClasses.tablet)

    WidthsByBreakpoint(
      mobile  = CutOutMobile.get(itemClasses.mobile),
      tablet  = CutOutTablet.get(itemClasses.tablet),
      desktop = CutOutDesktop.get(desktopClass)
    )
  }
}

object ContentWidths {

  sealed class ContentHinting (
    val mainContentWidths: WidthsByBreakpoint,
    val bodyContentWidths: WidthsByBreakpoint,
    val className: Option[String]
  )

  private val unused = WidthsByBreakpoint(None, None, None, None, None, None, None)

  object Inline     extends ContentHinting (MainMedia.Inline,     BodyMedia.Inline,     None)
  object Supporting extends ContentHinting (unused,               BodyMedia.Supporting, Some("element--supporting"))
  object Showcase   extends ContentHinting (MainMedia.Showcase,   BodyMedia.Showcase,   Some("element--showcase"))
  object Thumbnail  extends ContentHinting (unused,               BodyMedia.Thumbnail,  Some("element--thumbnail"))

  sealed trait ContentRelation

  object BodyMedia extends ContentRelation {
    val Inline = WidthsByBreakpoint(
      mobile =          Some(445.px),
      mobileLandscape = Some(605.px),
      phablet =         Some(620.px)) // tablet, desktop, leftCol and wide are also 620px

    val Supporting = WidthsByBreakpoint(
      mobile =          Some(445.px),
      mobileLandscape = Some(605.px),
      phablet =         Some(620.px), // tablet is also 620px
      desktop =         Some(300.px), // leftCol is also 300px
      wide =            Some(380.px))

    val Showcase = WidthsByBreakpoint(
      mobile =          Some(445.px),
      mobileLandscape = Some(605.px),
      phablet =         Some(620.px), // tablet and desktop are also 620px
      leftCol =         Some(780.px),
      wide =            Some(860.px))

    val Thumbnail = WidthsByBreakpoint(
      mobile =          Some(120.px), // mobileLandscape and tablet are also 120px
      tablet =          Some(140.px)) // desktop, leftCol and wide are also 140px
  }

  object MainMedia extends ContentRelation {
    val Inline = WidthsByBreakpoint(
      mobile =          Some(465.px),
      mobileLandscape = Some(645.px),
      phablet =         Some(620.px),
      tablet =          Some(700.px),
      desktop =         Some(620.px)) // leftCol and wide are also 620px

    val Showcase = WidthsByBreakpoint(
      mobile =          Some(465.px),
      mobileLandscape = Some(645.px),
      phablet =         Some(620.px),
      tablet =          Some(700.px),
      desktop =         Some(620.px),
      leftCol =         Some(780.px),
      wide =            Some(860.px))

    val FeatureShowcase = WidthsByBreakpoint(
      mobile =          Some(465.px),
      mobileLandscape = Some(645.px),
      phablet =         Some(725.px),
      tablet =          Some(965.px),
      desktop =         Some(1125.px),
      leftCol =         Some(1140.px),
      wide =            Some(1300.px))
  }

  object PictureMedia {
    // PictureMedia does not support hinting/weighting, so does not extend ContentRelation.
    val Inline = WidthsByBreakpoint(
      mobile =          Some(465.px),
      mobileLandscape = Some(645.px),
      phablet =         Some(685.px),
      tablet =          Some(700.px),
      desktop =         Some(940.px)) // leftCol and wide are also 940px
  }

  object GalleryMedia {
    val Inline = WidthsByBreakpoint(
      mobile          = Some(445.px),
      mobileLandscape = Some(610.px),
      phablet =         Some(620.px),
      tablet =          Some(700.px)) // desktop, leftCol, and wide are also 700px

    val Lightbox = WidthsByBreakpoint(
      mobile =          Some(465.px),
      mobileLandscape = Some(645.px),
      phablet =         Some(725.px),
      tablet =          Some(965.px),
      desktop =         Some(1065.px),
      leftCol =         Some(1225.px),
      wide =            Some(1920.px))
  }

  def getWidthsFromContentElement(hinting: ContentHinting, relation: ContentRelation): WidthsByBreakpoint = {
    relation match {
      case MainMedia => hinting.mainContentWidths
      case _ => hinting.bodyContentWidths }
  }
}

case class WidthsByBreakpoint(
  mobile:          Option[BrowserWidth] = None,
  mobileLandscape: Option[BrowserWidth] = None,
  phablet:         Option[BrowserWidth] = None,
  tablet:          Option[BrowserWidth] = None,
  desktop:         Option[BrowserWidth] = None,
  leftCol:         Option[BrowserWidth] = None,
  wide:            Option[BrowserWidth] = None
) {
  private val allBreakpoints: List[Breakpoint] = List(Wide, LeftCol, Desktop, Tablet, Phablet, MobileLandscape, Mobile)
  private val allWidths: List[Option[BrowserWidth]] = List(wide, leftCol, desktop, tablet, phablet, mobileLandscape, mobile)
  private val breakpoints: Seq[BreakpointWidth] = allBreakpoints zip allWidths collect {
      case (breakpoint, Some(width)) => BreakpointWidth(breakpoint, width)
    }

  private val MaximumMobileImageWidth = 620
  private val SourcesToEmitOnMobile = 3

  def sizes: String = breakpoints map {
    case BreakpointWidth(Mobile, imageWidth) =>
      imageWidth.toString

    case BreakpointWidth(breakpoint, imageWidth) =>
      s"(min-width: ${breakpoint.minWidth.get}px) $imageWidth"
  } mkString ", "


  def profiles: Seq[Profile] = (breakpoints flatMap {
    case BreakpointWidth(breakpoint, PixelWidth(pixels)) =>
      Seq(pixels)
    case BreakpointWidth(Mobile, _: PercentageWidth) =>
      // Percentage widths are not explicitly associated with any pixel widths that could be used with a srcset.
      // So we create a series of profiles by combining usable widths in the class with predefined sensible widths.
      val pixelWidths = breakpoints.collect { case BreakpointWidth(_,width: PixelWidth) => width.get }
      val widths: Seq[Int] = pixelWidths.dropWhile(_ > MaximumMobileImageWidth).take(SourcesToEmitOnMobile)
      widths ++ FaciaWidths.ExtraPixelWidthsForMediaMobile.map(_.get)
    case _ => Seq.empty
  })
  .distinct
  .map { (browserWidth: Int) =>
    Profile(width = Some(browserWidth))
  }
}

case class BreakpointWidth(breakpoint: Breakpoint, width: BrowserWidth)