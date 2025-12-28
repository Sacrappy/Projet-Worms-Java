package Backend;

import java.util.List;

public class Knife extends Weapon {
    private float range = 40.0f;
    private float arcAngle=60.0f;
    public Knife(){
        super("Knife", "A sharp blade used for close combat. Medium damage but requires close range range. Doesn't destroy terrain. Has infinite uses", 30);
        try {
            this.icon = loadBufferedImage("/Images/Weapons/Knife.png");
        } catch (Exception e) {
            this.icon = null;
        }
        
    }
    @Override
    public void use(ActorModel user, GameEngine gameEngine){

        double power = gameEngine.getCurrentPower();

        int finalDamage = (int) (30*power)/100;

        float angleDirection= (user.getFacing()==1)? 0.0f : 180.0f;

        List<Team> teams = gameEngine.getTeams();
        for (Team team : teams) {
            for (ActorModel target : team.getTeammates()) {
                if (target != user && target.isAlive()) {
                    if (isInHitbox(user, target, angleDirection)) {
                        target.sufferDamage(finalDamage);
                    }
                }
            }
        }
}
private boolean isInHitbox(ActorModel user, ActorModel target, float centerAngle) {
        double dx = target.getX() - user.getX();
        double dy = target.getY() - user.getY();
        double distance = Math.sqrt(dx * dx + dy * dy);

        if (distance <= range) {
            double angleToTarget = Math.toDegrees(Math.atan2(dy, dx));
            double angleDiff = Math.abs(angleToTarget - centerAngle + 180 +360) % 360 - 180;
            return angleDiff <= arcAngle / 2;
        }
        return false;
}
}
