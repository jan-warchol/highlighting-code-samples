// generate color data using templates/scala-diagram-input.template
val bg = new Color("bg", "103c48", 23)
val fg = new Color("fg", "adbcbc", 75)
val bgColors = List(bg, fg)
val fgColors = List(
    new Color("bg_0",    "103c48", 23),
    new Color("bg_1",    "174956", 28),
    new Color("bg_2",    "325b66", 36),
    new Color("red",     "fa5750", 60),
    new Color("orange",  "ed8649", 67),
    new Color("yellow",  "dbb32d", 75),
    new Color("green",   "75b938", 69),
    new Color("cyan",    "41c7b9", 73),
    new Color("blue",    "4695f7", 60),
    new Color("violet",  "af88eb", 64),
    new Color("magenta", "f275be", 66),
    new Color("dim_0",   "72898f", 56),
    new Color("fg_0",    "adbcbc", 75),
    new Color("fg_1",    "cad8d9", 85)
)
val title = "Selenized dark"

val plotW = 1000
val plotH = 600
val squareSize = 48
val squareHalf = squareSize/2.0
val lineWidth = 3
val margin = 125
val imgW = plotW + margin + lineWidth  // for rightmost border
val imgH = plotH + margin*1.5

// some programs (e.g. Gimp) don't handle text alignment correctly
val adjustAlignment = true
val adjX = if (adjustAlignment) lineWidth.toString else "0"
val adjY = if (adjustAlignment) squareHalf*0.4 else 0

class Color(val name: String, val hexString: String, val luminance: Int)

// choose either fg or bg, depending on provided luminance
def pickContrastingShade(luminance: Int) = {
    if (luminance > (bg.luminance + fg.luminance) / 2)
        "#"+bg.hexString
    else
        "#"+fg.hexString
}

def genSvg(bgColors: List[Color], fgColors: List[Color]) = {
    <svg version="1.1"
         xmlns="http://www.w3.org/2000/svg"
         xmlns:svg="http://www.w3.org/2000/svg"
         width={imgW.toString}
         height={imgH.toString}
         font-family="Signika, sans"
         font-size={(0.65*squareSize).toString+"px"} >
        <style>
            @font-face {{
                font-family: 'Signika';
                font-style: normal;
                font-weight: 400;
                src: local('Signika'), local('Signika-Regular'),
                  url(https://fonts.gstatic.com/s/signika/v6/q41y_9MUP_N8ipOH4ORRvw.woff2) format('woff2');
                unicode-range: U+0000-00FF, U+0131, U+0152-0153, U+02C6, U+02DA, U+02DC,
                  U+2000-206F, U+2074, U+20AC, U+2212, U+2215, U+E0FF, U+EFFD, U+F000;
            }}
            @font-face {{
                font-family: 'Signika';
                font-style: normal;
                font-weight: 700;
                src: local('Signika-Bold'),
                  url(https://fonts.gstatic.com/s/signika/v6/7M5kxD4eGxuhgFaIk95pBfk_vArhqVIZ0nv9q090hN8.woff2) format('woff2');
                unicode-range: U+0000-00FF, U+0131, U+0152-0153, U+02C6, U+02DA, U+02DC,
                  U+2000-206F, U+2074, U+20AC, U+2212, U+2215, U+E0FF, U+EFFD, U+F000;
            }}
        </style>
        <g transform={"translate("+margin+","+margin+")"} >
            <text x={(plotW/2.0).toString} y={(adjY/0.65-margin/2.0).toString}
                  text-anchor="middle" dominant-baseline="central"
                  font-size={(squareSize).toString+"px"} fill="#777" >
                { title }
            </text>
            <rect x={(-lineWidth/2.0).toString} y={(-lineWidth/2.0).toString}
                  width={(plotW+lineWidth).toString} height={(plotH+lineWidth).toString}
                  fill="#777" />
            <rect x="0" y="0" width={plotW.toString} height={plotH.toString} fill="#000" />
            { drawAxis() }
            { drawBackground(bgColors.sortBy(x => x.luminance)) }
            { drawSwatches(fgColors) }
        </g>
    </svg>
}

def drawAxis() = {
    val axisColor = "#777"
    val axisX = -squareSize
    val notchHalf = squareHalf/2.0

    <defs>
        <marker id="arrowhead" markerWidth="10" markerHeight="10" refX="0" refY="3" orient="auto" markerUnits="strokeWidth">
          <path d="M0,0 L0,6 L9,3 z" fill={axisColor} />
        </marker>
    </defs>

    <g stroke={axisColor} stroke-width={lineWidth.toString} >
        <line x1={(axisX).toString} y1={(plotH+margin/4.0).toString}
              x2={(axisX).toString} y2={(-margin/4.0).toString}
              marker-end="url(#arrowhead)" />
        <line x1={(axisX-notchHalf).toString} y1="0"
              x2={(axisX+notchHalf).toString} y2="0" />
        <line x1={(axisX-notchHalf).toString} y1={plotH.toString}
              x2={(axisX+notchHalf).toString} y2={plotH.toString} />
    </g>

    <text x={(axisX-1.5*notchHalf).toString} y={(plotH + adjY).toString}
          text-anchor="end" dominant-baseline="central"
          fill={axisColor} > 0 </text>
    <text x={(axisX-1.5*notchHalf).toString} y={adjY.toString}
          text-anchor="end" dominant-baseline="central"
          fill={axisColor} > 100 </text>
    <g transform={"translate("+(axisX-notchHalf)+","+plotH/2.0+")"} >
        <text x="0" y="0"
              text-anchor="middle"
              transform="rotate(-90)"
              fill={axisColor} > luminance </text>
    </g>
}

def drawBackground(colors: List[Color]) = {
    // we want the circles to have the same area as the squares
    val radius = scala.math.sqrt(4/scala.math.Pi) * squareHalf
    for {color <- colors} yield {
        <rect x="0" y="0"
              width={ plotW.toString }
              height={(((100-color.luminance)/100.0)*plotH).toString}
              fill={"#"+color.hexString} />
        <circle cx="0" cy={(((100-color.luminance)/100.0)*plotH).toString}
                r={radius.toString}
                fill={"#"+color.hexString}
                stroke="#777"
                stroke-width={lineWidth.toString} />
        <text x={adjX} y={(((100-color.luminance)/100.0)*plotH + adjY).toString}
              fill={pickContrastingShade(color.luminance)}
              text-anchor="middle" dominant-baseline="central" >
            { color.luminance.toString }
        </text>
        <text x={(radius*1.3).toString} y={(((98-color.luminance)/100.0)*plotH).toString}
              fill={pickContrastingShade(color.luminance)}
              text-anchor="begin" >
            { color.name }
        </text>

    }
}

def drawSwatches(colors: List[Color]) = {
    <g>{
        for {(color, i) <- colors.zipWithIndex} yield {
            val xcenter = (i+1.7)*plotW/(colors.length+1.5)
            val ycenter = color.luminance*plotH/100.0

            <!-- group is translated to the center of square with some flipping for easier translation -->
            <g transform={"scale(1,-1)" +
                          "translate("+xcenter.toString+","+ycenter.toString+")" +
                          "scale(1,-1)" +
                          "translate(0, "+plotH.toString+")"} >
                <g stroke={"#"+bg.hexString} stroke-width={lineWidth.toString} >
                    <rect x={(-squareHalf).toString}
                          y={(-squareHalf).toString}
                          width={squareSize.toString}
                          height={squareSize.toString}
                          fill={"#"+color.hexString}
                          stroke={"#"+bg.hexString}
                          stroke-width={lineWidth.toString} />
                </g>

                <text x={adjX} y={adjY.toString}
                      fill={pickContrastingShade(color.luminance)}
                      text-anchor="middle" dominant-baseline="central" >
                    { color.luminance.toString }
                </text>

                <text x={(-squareSize*0.8).toString} y={adjY.toString}
                      fill={"#"+color.hexString}
                      text-anchor="end"
                      dominant-baseline="central"
                      font-weight="bold"
                      transform={"rotate(-90)"} >
                    { color.name }
                </text>
            </g>
        }
    }</g>
}

println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")

println(genSvg(bgColors, fgColors))

