package com.zzstudio
{
	import flash.display.Bitmap;
	import flash.display.BitmapData;
	import flash.display.DisplayObject;
	import flash.display.Loader;
	import flash.display.MovieClip;
	import flash.display.PNGEncoderOptions;
	import flash.display.Sprite;
	import flash.events.Event;
	import flash.geom.Matrix;
	import flash.geom.Rectangle;
	import flash.system.ApplicationDomain;
	import flash.system.LoaderContext;
	
	import zzsdk.utils.FileUtil;

	public class Main extends Sprite
	{
		private var graphicLayers:Array = [];

		public function Main()
		{
			var lc:LoaderContext = new LoaderContext(false, ApplicationDomain.currentDomain);
			var loader:Loader = new Loader;
			loader.contentLoaderInfo.addEventListener(Event.COMPLETE, function():void
			{
				var mc:MovieClip = loader.content as MovieClip
				parseMC(mc);
//				drawNextGraphics();
			});
			lc.allowCodeImport = true;
			loader.loadBytes(FileUtil.open("kitty.swf"), lc);
		}

		private function parseMC(mc:MovieClip):void
		{
			mc.stop();
			for (var i:int = 0; i < mc.numChildren; i++)
			{
				parseActionLayer(mc.getChildAt(i) as MovieClip, mc.currentFrame);
			}
		}

		private function parseActionLayer(mc:MovieClip, frame:int):void
		{
			mc.stop();
			for (var i:int = 0; i < mc.numChildren; i++)
			{
				parseAnimationLayer(mc.getChildAt(i) as MovieClip, mc.currentFrame);
			
				var layer:MovieClip = mc.getChildAt(i) as MovieClip;
				trace(layer.transform.matrix)
			}
		}

		private function parseAnimationLayer(mc:MovieClip, currentFrame:int):void
		{
			mc.stop();
			trace(mc.name);
			if (mc.numChildren == 1)
			{
				parseGraphicsLayer(mc, mc.name);
			}
			else
			{
				for (var i:int = 0; i < mc.numChildren; i++)
				{
					parseGraphicsLayer(mc.getChildAt(i) as MovieClip, mc.name + "/L" + i);
				}
			}
		}

		private function parseGraphicsLayer(mc:MovieClip, name:String):void
		{
			graphicLayers.push({mc: mc, name: name});
		}

		private function drawNextGraphics():void
		{
			if (graphicLayers.length == 0)
			{
				return;
			}
			doDrawGraphicsLayer(graphicLayers.shift());
		}

		private function doDrawGraphicsLayer(obj):void
		{
			var mc:MovieClip = obj.mc;
			var name:String = obj.name;
			mc.gotoAndStop(0);
			addEventListener(Event.ENTER_FRAME, function():void
			{
				var bitmap:BitmapData = drawToBitmap(mc);
				if (bitmap != null)
				{
					FileUtil.save(bitmap.encode(bitmap.rect, new PNGEncoderOptions), "output/" + name + "/" + format(mc.currentFrame) + ".png");
					bitmap.dispose()
				}

				mc.nextFrame();
				if (mc.currentFrame == mc.totalFrames)
				{
					removeEventListener(Event.ENTER_FRAME, arguments.callee);
					drawNextGraphics()
				}
			});
		}

		private function format(i:int):String
		{
			if (i < 10)
				return "00" + i;
			if (i < 100)
				return "0" + i;
			return i + "";
		}

		private var scale:int = 2;
		private var lastChild:*;

		private function drawToBitmap(mc:MovieClip):BitmapData
		{
			if (mc.numChildren == 0)
			{
				return null;
			}
			lastChild = mc.getChildAt(0);
			var border:Rectangle = mc.getBounds(mc);
			if (int(border.width) == 9 && int(border.height) == 8)
			{
				return null;
			}
			var bitmap:BitmapData = new BitmapData(border.width * scale, border.height * scale, true, 0);
			bitmap.draw(mc, new Matrix(scale, 0, 0, scale, -border.x * scale, -border.y * scale));
			return bitmap;
		}
	}
}
