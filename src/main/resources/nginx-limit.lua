--
-- User: Nicky_chin
-- Date: 2017/12/18
-- Time: 15:46
--

local locks = require "resty.lock" --lock模块

local limit = 3 --限流大小

local function acquire()

    if limit <= 0 then
        return 0
    end

    local lock = locks:new("locks")
    local elapsed, err = lock:lock("limit_key") --互斥锁
    local limit_counter = ngx.shared.limit_counter --计数器

    local key = "ip" .. os.time()
    local current = limit_counter:get(key)

-- ~=nil  不等于空
    if current ~= nil and current + 1 > limit then --如果超出限流大小
        lock:unlock()
        return 0
    end

    if current == nil then
        limit_counter:set(key, 1, 1) --第一次需要设置过期时间，设置key的值为1，过期时间为1秒
    else
        limit_counter:incr(key, 1) --第二次开始递增加1
    end

    lock:unlock()
    return 1
end

local rev = acquire()
ngx.log(ngx.ERR, rev)
if rev ~= 1 then
    ngx.say("限流了")
else
    ngx.say("访问通过")
end
