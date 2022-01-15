# ##### BEGIN GPL LICENSE BLOCK #####
#
#  This program is free software; you can redistribute it and/or
#  modify it under the terms of the GNU General Public License
#  as published by the Free Software Foundation; either version 2
#  of the License, or (at your option) any later version.
#
#  This program is distributed in the hope that it will be useful,
#  but WITHOUT ANY WARRANTY; without even the implied warranty of
#  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#  GNU General Public License for more details.
#
#  You should have received a copy of the GNU General Public License
#  along with this program; if not, write to the Free Software Foundation,
#  Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
#
# ##### END GPL LICENSE BLOCK #####

# <pep8 compliant>

bl_info = {
    'name': 'Index Visualiser (BMesh)',
    'author': 'Bartius Crouch, CoDEmanX',
    'version': (2, 6, 14),
    'blender': (2, 65, 0),
    'location': 'View3D > Properties panel > Mesh Display tab (edit-mode)',
    'warning': '', # used for warning icon and text in addons panel
    'description': 'Display the indices of vertices, edges and faces '\
        'in the 3d-view',
    'wiki_url': 'http://wiki.blender.org/index.php/Extensions:2.5/Py/'\
        'Scripts/3D_interaction/Index_Visualiser',
    'tracker_url': 'http://projects.blender.org/tracker/index.php?'\
        'func=detail&aid=21557',
    'category': '3D View'}


"""
Display the indices of vertices, edges and faces in the 3d-view.

How to use:
- Select a mesh and go into editmode
- Display the properties panel (N-key)
- Go to the Mesh Display tab, it helps to fold the tabs above it
- Press the 'Visualise indices button'

"""

import bpy
import bgl
import blf
import mathutils
import bmesh


# calculate locations and store them as ID property in the mesh
def draw_callback_px(self, context):
    # polling
    if context.mode != "EDIT_MESH":
        return

    # get screen information
    region = context.region
    mid_x = region.width / 2
    mid_y = region.height / 2
    width = region.width
    height = region.height
    
    # get matrices
    view_mat = context.space_data.region_3d.perspective_matrix
    ob_mat = context.active_object.matrix_world
    total_mat = view_mat * ob_mat
        
    blf.size(0, 13, 72)
    
    def draw_index(r, g, b, index, center):
        
        vec = total_mat * center # order is important
        # dehomogenise
        vec = mathutils.Vector((vec[0] / vec[3], vec[1] / vec[3], vec[2] / vec[3]))
        x = int(mid_x + vec[0] * width / 2)
        y = int(mid_y + vec[1] * height / 2)
        
        bgl.glColor3f(r, g, b)
        blf.position(0, x, y, 0)
        blf.draw(0, str(index))


    scene = context.scene
    me = context.active_object.data
    bm = bmesh.from_edit_mesh(me)
    
    if scene.live_mode:
        me.update()
    
    if scene.display_vert_index:
        for v in bm.verts:
            if not v.hide and \
            (v.select or not scene.display_sel_only):
                ## CoDEmanx: bm.verts.index_update()?
                draw_index(1.0, 1.0, 1.0, v.index, v.co.to_4d())
    if scene.display_edge_index:
        for e in bm.edges:
            if not e.hide and \
            (e.select or not scene.display_sel_only):
                v1 = e.verts[0].co
                v2 = e.verts[1].co
                loc = v1 + ((v2 - v1) / 2)
                draw_index(1.0, 1.0, 0.0, e.index, loc.to_4d())
    if scene.display_face_index:
        for f in bm.faces:
            if not f.hide and \
            (f.select or not scene.display_sel_only):
                draw_index(1.0, 0.0, 0.5, f.index, f.calc_center_median().to_4d())


# operator
class IndexVisualiser(bpy.types.Operator):
    bl_idname = "view3d.index_visualiser"
    bl_label = "Index Visualiser"
    bl_description = "Toggle the visualisation of indices"
    
    _handle = None
    
    @classmethod
    def poll(cls, context):
        return context.mode=="EDIT_MESH"
    
    def modal(self, context, event):
        if context.area:
            context.area.tag_redraw()

        # removal of callbacks when operator is called again
        if context.scene.display_indices == -1:
            bpy.types.SpaceView3D.draw_handler_remove(self._handle, 'WINDOW')
            context.scene.display_indices = 0
            return {"CANCELLED"}
        
        return {"PASS_THROUGH"}
    
    def invoke(self, context, event):
        if context.area.type == "VIEW_3D":
            if context.scene.display_indices < 1:
                # operator is called for the first time, start everything
                context.scene.display_indices = 1
                self._handle = bpy.types.SpaceView3D.draw_handler_add(draw_callback_px,
                    (self, context), 'WINDOW', 'POST_PIXEL')
                context.window_manager.modal_handler_add(self)
                return {"RUNNING_MODAL"}
            else:
                # operator is called again, stop displaying
                context.scene.display_indices = -1
                return {'RUNNING_MODAL'}
        else:
            self.report({"WARNING"}, "View3D not found, can't run operator")
            return {"CANCELLED"}


# defining the panel
def menu_func(self, context):
    self.layout.separator()
    col = self.layout.column(align=True)
    col.operator(IndexVisualiser.bl_idname, text="Visualize indices")
    row = col.row(align=True)
    row.active = (context.mode=="EDIT_MESH" and \
        context.scene.display_indices==1)
    row.prop(context.scene, "display_vert_index", toggle=True)
    row.prop(context.scene, "display_edge_index", toggle=True)
    row.prop(context.scene, "display_face_index", toggle=True)
    row = col.row(align=True)
    row.active = context.mode == "EDIT_MESH" and \
                 context.scene.display_indices == 1
    row.prop(context.scene, "display_sel_only")
    #row.prop(context.scene, "live_mode")


def register_properties():
    bpy.types.Scene.display_indices = bpy.props.IntProperty(
        name="Display indices",
        default=0)
    #context.scene.display_indices = 0
    bpy.types.Scene.display_sel_only = bpy.props.BoolProperty(
        name="Selected only",
        description="Only display indices of selected vertices/edges/faces",
        default=True)
    bpy.types.Scene.display_vert_index = bpy.props.BoolProperty(
        name="Vertices",
        description="Display vertex indices", default=True)
    bpy.types.Scene.display_edge_index = bpy.props.BoolProperty(
        name="Edges",
        description="Display edge indices")
    bpy.types.Scene.display_face_index = bpy.props.BoolProperty(
        name="Faces",
        description="Display face indices")
    bpy.types.Scene.live_mode = bpy.props.BoolProperty(
        name="Live",
        description="Toggle live update of the selection, can be slow",
        default=False)
        
def unregister_properties():
    del bpy.types.Scene.display_indices
    del bpy.types.Scene.display_sel_only
    del bpy.types.Scene.display_vert_index
    del bpy.types.Scene.display_edge_index
    del bpy.types.Scene.display_face_index
    del bpy.types.Scene.live_mode


def register():
    register_properties()
    bpy.utils.register_class(IndexVisualiser)
    bpy.types.VIEW3D_PT_view3d_meshdisplay.append(menu_func)


def unregister():
    bpy.utils.unregister_class(IndexVisualiser)
    unregister_properties()
    bpy.types.VIEW3D_PT_view3d_meshdisplay.remove(menu_func)


if __name__ == "__main__":
    register()
