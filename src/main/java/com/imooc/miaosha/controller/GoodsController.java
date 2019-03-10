package com.imooc.miaosha.controller;

import com.imooc.miaosha.domain.MiaoshaUser;
import com.imooc.miaosha.redis.GoodsKey;
import com.imooc.miaosha.redis.RedisService;
import com.imooc.miaosha.result.Result;
import com.imooc.miaosha.service.GoodsService;
import com.imooc.miaosha.service.MiaoshaUserService;
import com.imooc.miaosha.service.UserService;
import com.imooc.miaosha.vo.GoodsDetailVo;
import com.imooc.miaosha.vo.GoodsVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.spring4.context.SpringWebContext;
import org.thymeleaf.spring4.view.ThymeleafViewResolver;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RequestMapping("/goods")
@Controller
public class GoodsController {
    @Autowired
    private RedisService redisService;
    @Autowired
    private MiaoshaUserService userService;
    @Autowired
    private GoodsService goodsService;
    @Autowired
    private ThymeleafViewResolver thymeleafViewResolver;
    @Autowired
    private ApplicationContext applicationContext;

//    @RequestMapping("/to_list")
//    public String list(Model model, HttpServletResponse response,
//                         @CookieValue(value = MiaoshaUserService.COOKIE_NAME_TOKEN,required = false) String cookieToken,
//                         @RequestParam(value = MiaoshaUserService.COOKIE_NAME_TOKEN,required = false) String paramToken
//                         )
//    {
//        if(StringUtils.isEmpty(cookieToken)&&StringUtils.isEmpty(paramToken)){
//            return "login";
//        }
//        String token=StringUtils.isEmpty(paramToken)?cookieToken:paramToken;
//        MiaoshaUser user=userService.getByToken(response,token);
//        model.addAttribute("user",user);
//        return "goods_list";
//    }

    //页面级缓存
    @RequestMapping(value = "/to_list",produces = "text/html")
    @ResponseBody
    public String list(HttpServletRequest request,HttpServletResponse response,Model model, MiaoshaUser user){
        model.addAttribute("user",user);
        //取缓存
        String html=redisService.get(GoodsKey.getGoodsList,"",String.class);
        if(!StringUtils.isEmpty(html)){
            return html;
        }
        //查询商品列表
        List<GoodsVo> goodsList=goodsService.listGoodsVo();
        model.addAttribute("goodsList",goodsList);
//        return "goods_list";

        SpringWebContext ctx=new SpringWebContext(request,response,request.getServletContext(),request.getLocale(),model.asMap(),applicationContext);
        //手动渲染
        html=thymeleafViewResolver.getTemplateEngine().process("goods_list",ctx);
        if(!StringUtils.isEmpty(html)){
            //将渲染的页面保存到缓存中
            redisService.set(GoodsKey.getGoodsList,"",html);
        }
        return html;
    }

    @RequestMapping(value = "/to_detail/{goodsId}",produces = "text/html")
    @ResponseBody
    public String detail(HttpServletRequest request,HttpServletResponse response,Model model, MiaoshaUser user, @PathVariable("goodsId") long goodsId){
        model.addAttribute("user",user);

        //取缓存
        String html=redisService.get(GoodsKey.getGoodsDetail,""+goodsId,String.class);
        if(!StringUtils.isEmpty(html)){
            return html;
        }

        GoodsVo goods=goodsService.getGoodsVoByGoodsId(goodsId);
        model.addAttribute("goods",goods);
        //秒杀开始以及结束时间
        long startAt=goods.getStartDate().getTime();
        long endAt=goods.getEndDate().getTime();
        long now=System.currentTimeMillis();
        int miaoshaStatus=0;
        int remainSeconds=0;
        if(now<startAt){//秒杀还没开始
            miaoshaStatus=0;
            remainSeconds=(int)(startAt-now)/1000;
        }else if(now>endAt){//秒杀已经结束
            miaoshaStatus=2;
            remainSeconds=-1;
        }else{//秒杀正在进行中
            miaoshaStatus=1;
            remainSeconds=0;
        }
        model.addAttribute("miaoshaStatus",miaoshaStatus);
        model.addAttribute("remainSeconds",remainSeconds);
        //return "goods_detail";

        //手动渲染
        SpringWebContext ctx=new SpringWebContext(request,response,request.getServletContext(),request.getLocale(),model.asMap(),applicationContext);
        //手动渲染
        html=thymeleafViewResolver.getTemplateEngine().process("goods_detail",ctx);
        if(!StringUtils.isEmpty(html)){
            //将渲染的页面保存到缓存中
            redisService.set(GoodsKey.getGoodsDetail,""+goodsId,html);
        }
        return html;
    }

    //页面静态化
    @RequestMapping(value = "/to_detail2/{goodsId}")
    @ResponseBody
    public Result<GoodsDetailVo> detail2(HttpServletRequest request, HttpServletResponse response, Model model, MiaoshaUser user, @PathVariable("goodsId") long goodsId){

        GoodsVo goods=goodsService.getGoodsVoByGoodsId(goodsId);
        model.addAttribute("goods",goods);
        //秒杀开始以及结束时间
        long startAt=goods.getStartDate().getTime();
        long endAt=goods.getEndDate().getTime();
        long now=System.currentTimeMillis();
        int miaoshaStatus=0;
        int remainSeconds=0;
        if(now<startAt){//秒杀还没开始
            miaoshaStatus=0;
            remainSeconds=(int)(startAt-now)/1000;
        }else if(now>endAt){//秒杀已经结束
            miaoshaStatus=2;
            remainSeconds=-1;
        }else{//秒杀正在进行中
            miaoshaStatus=1;
            remainSeconds=0;
        }
        GoodsDetailVo vo=new GoodsDetailVo();
        vo.setGoods(goods);
        vo.setUser(user);
        vo.setRemainSeconds(remainSeconds);
        vo.setMiaoshaStatus(miaoshaStatus);
        return Result.success(vo);
    }
}
