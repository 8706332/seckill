/**
 *模块化js类似面向对象设计
 * secKillObj就是一个json对象 ，这个对象可以有若干个属性
 *  url和fun就是secKillObj对象的属性，它们也是json对象
 *  url对象中可以用若干个属性，这个对象的属性主要函数类型为主，用于返回url地址路径，可以起到地址路径重用的特点
 *  fun对象中可以用若干个属性，这个对象的属性主要函数类型为主，用于完成秒杀的相关业务逻辑的控制
 */

var secKillObj = {

    url: {
        getSystemTime: function () {
            return "/getSystemTime";
        },

        getRandomName: function (goodsId) {
            return "/getRandomName?id=" + goodsId;
        },

        secKill: function (goodsId, randomName) {
            return "/secKill?goodsId=" + goodsId + "&randomName=" + randomName;
        },

        getOrdersResult: function (goodsId) {
            return "/getOrdersResult?goodsId=" + goodsId;
        }
    },

    fun: {
        /**
         * 初始化秒杀函数，用来控制抢购按钮是否可用
         * @param goodsId
         * @param startTime
         * @param endTime
         */
        initSecKill: function (goodsId, startTime, endTime) {
            $.ajax({
                url: secKillObj.url.getSystemTime(),//获取系统时间
                //data: "",
                dataType: "json",//响应的数据格式可以为text，json，script
                type: "post",
                success: function (data) {
                    //判断是否成功
                    if (data.code != 1) {
                        alert(data.message);
                        return false;
                    }
                    //和系统时间进行比较
                    var systemTime = data.data;

                    if (systemTime < startTime) {
                        secKillObj.fun.secKillCountDown(goodsId, startTime);
                        return false;
                    }

                    if (systemTime > endTime) {
                        $("#seckillBtn").html("<span style='color:red;'>" + "活动已经结束" + "</span>");
                        return false;
                    }

                    secKillObj.fun.doSecKill(goodsId);

                },
                error: function () {
                    alert("您的网络异常，请稍后重试")
                }
            })

        },

        /**
         * 秒杀商品倒计时的方法
         * @param goodsId
         * @param startTime
         *
         * ps:此插件使用浏览器的时间为倒计时时间，实际工作时慎重使用
         */
        secKillCountDown: function (goodsId, startTime) {
            //使用jquery的倒计时插件实现倒计时
            /* + 1000 防止时间偏移 这个没有太多意义，因为我们并不知道客户端和服务器的时间偏移
            这个插件简单了解，实际项目不会以客户端时间作为倒计时的，所以我们在服务器端还需要验证*/

            //定义一个时间对象作为倒计时的目标时间
            //ps: *1 是将数据转换成数字
            var killTime = new Date(startTime * 1);
            /**
             * 使用任意一个jquery对象来调用countdown方法实现倒计时
             * killTime 倒计时时间
             * 参数2为 回调函数 ，利用它来更新剩余时间
             * countdown方法每秒钟会调用回调函数一次
             */
            $("#seckillBtn").countdown(killTime, function (event) {
                //时间格式
                var format = event.strftime('距秒杀开始还有: %D天 %H时 %M分 %S秒');
                $("#seckillBtn").html("<span style='color:red;'>" + format + "</span>");
            }).on('finish.countdown', function () {
                //倒计时结束后回调事件，已经开始秒杀，用户可以进行秒杀了，有两种方式：

                //1、刷新当前页面
                //location.reload();

                //或者2、调用秒杀开始的函数
                secKillObj.fun.doSecKill(goodsId);
            });
        },

        /**
         * 秒杀活动开始的函数
         */
        doSecKill: function (goodsId) {
            $("#seckillBtn").html('<input type="button" value="立即抢购" id="secKillBtn">');

            $("#secKillBtn").bind("click", function () {
                //点击一次后，设置按钮不可用
                //这里不能100%拦截所有的重复请求
                $(this).attr("disabled", true);
                //为了避免刷单手动拼接请求，需要获取商品的随机名称

                $.ajax({
                    url: secKillObj.url.getRandomName(goodsId),
                    dataType: "json",
                    type: "post",
                    success: function (data) {
                        if (data.code != "1") {
                            alert(data.message);
                            return false;
                        }

                        var randomName = data.data;
                        //如果获取成功，调用商品秒杀函数
                        secKillObj.fun.secKill(goodsId, randomName);
                    },
                    error: function () {

                        alert("网络出现异常，请稍后重试")
                    }
                })
            })
        },

        /**
         * 商品秒杀函数
         * @param goodsId
         * @param randomName
         */
        secKill(goodsId, randomName) {

            $.ajax({
                url: secKillObj.url.secKill(goodsId, randomName),
                dataType: "json",
                type: "post",
                success: function (data) {
                    if (data.code != "1") {
                        alert(data.message);
                        return false;
                    }
                    //如果成功，获取下单后的结果
                    secKillObj.fun.getOrdersResult(goodsId);
                },
                error: function () {
                    alert("网络出现异常，请稍后重试")
                }
            })
        },

        getOrdersResult: function (goodsId) {
            $.ajax({
                url: secKillObj.url.getOrdersResult(goodsId),
                dataType: "json",
                type: "get",
                success: function (data) {
                    if (data.code != "1") {
                        //如果没有立刻获取到结果，需要3秒后延迟获取
                        window.setTimeout("secKillObj.fun.getOrdersResult(" + goodsId + ")", 3000);
                        return false;
                    }

                    var orderId = data.data.id;
                    var orderPrice = data.data.orderMoney;

                    $("#seckillBtn").html('<span style="color: red">下单成功：共计' + orderPrice + ' 元 <a href="/pay?orderId="' + orderId + '>立即支付</a></span>')
                },
                error: function () {
                    alert("网络出现异常，请稍后重试")
                }
            })
        }
    }
};