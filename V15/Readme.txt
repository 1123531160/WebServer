解决传递中文问题
上一个版本中,我们知道通过页面form表单提交数据.当我们使用GET形式提交数据时,数据会被包含在URL中对
请求发送过来,而此时数据会体现在请求行的抽象路径部分.
由于HTTP协议要求,请求的请求行和消息头是字符串,并且使用的字符集是ISO8859-1,这是一个欧洲的字符集
因此这部分的内容不能直接出现中文.

解决办法
1:浏览器会先将中文内容按照页面指定的字符集(页面中head里<meta charset="UTF-8">这里指定)将提交的
中文内容转换为一组字节
2:将每个字节以2位16进制式表示
3:将每个字节对应的2位16进制前面加上%,用于表明这是一个16进制形式的1字节内容(URL规定的格式)
从而传递给服务端
4:服务端只需要将这些%XX内容反向解析即可得到中文.
注:由于16进制对应的字符为:0-9 A-F这些是ISO8859-1支持的字符,所有用这样的方式传递中文.


支持POST形式的表单提交
form表单上如果method属性的值为post,则表单提交后,输入框输入的内容会体现在请求的消息正文中,
实际上此时消息正文中的内容还是文字,并且格式与get形式提交时抽象路径中?右侧内容的格式是一致的.

实现:
完成HttpRequest中解析消息正文的方法.
