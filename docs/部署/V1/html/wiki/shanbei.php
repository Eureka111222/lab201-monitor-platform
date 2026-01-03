<?php
$imgurl = json_decode(file_get_contents("https://apiv3.shanbay.com/weapps/dailyquote/quote"), true)['poster_img_urls'][0];
$img = file_get_contents($imgurl);
file_put_contents("shanbei.jpg", $img);
list($src_w,$src_h)=getimagesize("shanbei.jpg");
$source_image = imagecreatefromjpeg("shanbei.jpg");
$cropped_image = imagecreatetruecolor($src_w, $src_h/4);
imagecopy($cropped_image, $source_image, 0, 0, 0, $src_h*3/4, $src_w, $src_h/4);
$target_image = imagecreatetruecolor($src_w/2, $src_h/8);
imagecopyresampled($target_image, $cropped_image, 0, 0, 0, 0, $src_w/2, $src_h/8, $src_w, $src_h/4);
header('Content-Type: image/jpeg');
imagejpeg($target_image);
imagedestroy($source_image);
imagedestroy($cropped_image);
imagedestroy($target_image);
// phpinfo()
?>